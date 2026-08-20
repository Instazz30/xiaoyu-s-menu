package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.entity.ImageAsset;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.GroupMemberMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.ImageAssetMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuSectionMapper;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_WIDTH = 1920;
    private static final int THUMBNAIL_WIDTH = 300;

    private final ImageAssetMapper imageMapper;
    private final MenuItemMapper itemMapper;
    private final MenuRecipeMapper recipeMapper;
    private final MenuSectionMapper sectionMapper;
    private final PermissionService permissionService;
    private final GroupMemberMapper memberMapper;
    private final OssStorageService storageService;

    public ImageService(ImageAssetMapper imageMapper,
                        MenuItemMapper itemMapper,
                        MenuRecipeMapper recipeMapper,
                        MenuSectionMapper sectionMapper,
                        PermissionService permissionService,
                        GroupMemberMapper memberMapper,
                        OssStorageService storageService) {
        this.imageMapper = imageMapper;
        this.itemMapper = itemMapper;
        this.recipeMapper = recipeMapper;
        this.sectionMapper = sectionMapper;
        this.permissionService = permissionService;
        this.memberMapper = memberMapper;
        this.storageService = storageService;
    }

    /**
     * 上传菜品图片（管理员与普通成员均可）。
     * 管理员上传直接通过并绑定；成员上传进入待审核，审核通过后才绑定。
     * 成员从相册上传还需单独拥有相册权限，拍照上传不需要该权限。
     */
    @Transactional
    public ImageAsset upload(Long recipeId, Long itemId, MultipartFile file, Long userId, String source) {
        MenuRecipe recipe = permissionService.requireMemberOfRecipe(recipeId, userId);
        MenuItem item = null;
        if (itemId != null) {
            item = itemMapper.selectById(itemId);
            if (item == null) {
                throw new BusinessException(10004, "菜品不存在或已删除");
            }
        }

        var member = memberMapper.selectByGroupAndUser(recipe.getGroupId(), userId);
        boolean isAdmin = member != null && "admin".equals(member.getRole());
        // 相册权限只控制成员能否从相册选图，必须在图片处理和 OSS 上传前完成校验
        if ("album".equals(source) && !isAdmin) {
            if (member == null || member.getAlbumPermission() == null
                    || member.getAlbumPermission() != 2) {
                throw new BusinessException(403, "没有相册上传权限，请先在小组详情中申请");
            }
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        String detectedFormat = "unknown";

        if (file.isEmpty()) {
            throw new BusinessException(10005, "文件为空");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new BusinessException(10007, "图片大小不能超过10MB");
        }

        try {
            InputStream rawStream = file.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(rawStream);
            bis.mark(16);
            detectedFormat = validateMagicBytes(bis);
            bis.reset();

            String outExt = "jpg";
            log.info("图片上传开始 — 文件名: {}, 大小: {}KB, ContentType: {}, 检测格式: {} → 输出: {}",
                    originalFilename, fileSize / 1024, contentType, detectedFormat, outExt);

            String baseName = UUID.randomUUID().toString();
            String outFilename = baseName + "." + outExt;
            String thumbFilename = baseName + "_thumb." + outExt;

            BufferedImage image = Thumbnails.of(bis)
                    .scale(1.0)
                    .outputFormat(outExt)
                    .asBufferedImage();

            int w = image.getWidth();
            int h = image.getHeight();

            BufferedImage outputImage = image;
            if (w > MAX_WIDTH || h > MAX_WIDTH) {
                outputImage = Thumbnails.of(image)
                        .size(MAX_WIDTH, MAX_WIDTH)
                        .outputFormat(outExt)
                        .asBufferedImage();
            }

            BufferedImage thumbnail = Thumbnails.of(image)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_WIDTH)
                    .outputFormat(outExt)
                    .asBufferedImage();

            byte[] originalBytes = toJpegBytes(outputImage);
            byte[] thumbnailBytes = toJpegBytes(thumbnail);
            String objectPrefix = "recipes/" + recipeId + "/images/";
            CompletableFuture<String> originalUpload = CompletableFuture.supplyAsync(() ->
                    storageService.upload(objectPrefix + outFilename, originalBytes, "image/jpeg"));
            CompletableFuture<String> thumbnailUpload = CompletableFuture.supplyAsync(() ->
                    storageService.upload(objectPrefix + thumbFilename, thumbnailBytes, "image/jpeg"));
            String originalUrl;
            String thumbnailUrl;
            try {
                CompletableFuture.allOf(originalUpload, thumbnailUpload).join();
                originalUrl = originalUpload.join();
                thumbnailUrl = thumbnailUpload.join();
            } catch (CompletionException e) {
                log.error("图片上传 OSS 失败 — 文件名: {}, 检测格式: {}", originalFilename, detectedFormat,
                        e.getCause() != null ? e.getCause() : e);
                throw new BusinessException(10009, "图片存储失败，请稍后重试");
            }

            ImageAsset asset = new ImageAsset();
            asset.setRecipeId(recipeId);
            asset.setGroupId(recipe.getGroupId());
            asset.setUploaderId(userId);
            // 相册权限只控制成员能否从相册选图；图片审核只由角色决定：管理员免审，成员必审
            boolean autoApprove = isAdmin;
            if (autoApprove) {
                asset.setReviewStatus("approved");
                asset.setReviewerId(userId);
                asset.setReviewedAt(LocalDateTime.now());
            } else {
                asset.setReviewStatus("pending");
            }
            asset.setItemId(itemId);
            asset.setOriginalUrl(originalUrl);
            asset.setThumbnailUrl(thumbnailUrl);
            asset.setFileType(outExt);
            asset.setFileSize(fileSize);
            asset.setWidth(w);
            asset.setHeight(h);
            asset.setUploadStatus("success");
            imageMapper.insert(asset);

            if (autoApprove && item != null) {
                item.setImageId(asset.getId());
                item.setImageStatus("uploaded");
                itemMapper.update(item);
            }

            log.info("图片上传成功{} — 菜品: {}, 尺寸: {}x{}, 输出: {}",
                    autoApprove ? "（直通）" : "（待审核）",
                    item != null ? item.getName() : "小碗菜自由图", w, h, outFilename);
            return asset;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片处理失败 — 文件名: {}, 大小: {}KB, 检测格式: {}, 异常: {}",
                    originalFilename, fileSize / 1024, detectedFormat,
                    e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            throw new BusinessException(10005, "图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 替换菜品展示图（仅管理员）：上传一张新图进入待审核，审核通过后绑定并替换旧图。
     */
    @Transactional
    public ImageAsset replace(Long recipeId, Long itemId, MultipartFile file, Long userId) {
        permissionService.requireAdminOfRecipe(recipeId, userId);
        return upload(recipeId, itemId, file, userId, null);
    }

    /**
     * 审核图片（仅管理员）。通过后自动绑定为该菜品的展示图；不通过必须填写原因。
     */
    @Transactional
    public void review(Long imageId, boolean approve, String note, Long reviewerId) {
        ImageAsset asset = imageMapper.selectById(imageId);
        if (asset == null) {
            throw new BusinessException(10008, "图片不存在或已删除");
        }
        permissionService.requireAdmin(asset.getGroupId(), reviewerId);
        if (!"pending".equals(asset.getReviewStatus())) {
            throw new BusinessException(400, "该图片已审核，请勿重复操作");
        }
        if (!approve && (note == null || note.isBlank())) {
            throw new BusinessException(400, "审核不通过时必须填写原因");
        }

        asset.setReviewStatus(approve ? "approved" : "rejected");
        asset.setReviewerId(reviewerId);
        asset.setReviewedAt(LocalDateTime.now());
        asset.setReviewNote(note);
        imageMapper.update(asset);

        if (approve && asset.getItemId() != null) {
            MenuItem item = itemMapper.selectById(asset.getItemId());
            if (item != null) {
                item.setImageId(asset.getId());
                item.setImageStatus("uploaded");
                itemMapper.update(item);
            }
        }
    }

    /** 下架菜品当前展示图（仅管理员） */
    @Transactional
    public void unbind(Long recipeId, Long itemId, Long userId) {
        permissionService.requireAdminOfRecipe(recipeId, userId);
        MenuItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(10004, "菜品不存在或已删除");
        }
        item.setImageId(null);
        item.setImageStatus("pending");
        itemMapper.update(item);
    }

    /** 删除小碗菜自由图（不对应具体菜品） */
    @Transactional
    public void deleteXiaowan(Long recipeId, Long imageId, Long userId) {
        MenuRecipe recipe = permissionService.requireMemberOfRecipe(recipeId, userId);
        ImageAsset img = imageMapper.selectById(imageId);
        if (img == null || !recipeId.equals(img.getRecipeId())) {
            throw new BusinessException(10008, "图片不存在或已删除");
        }
        imageMapper.deleteById(imageId);
    }

    /** 小组待审核图片列表（仅管理员） */
    public List<Map<String, Object>> pendingImages(Long groupId, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        return imageMapper.selectPendingByGroup(groupId);
    }

    public Map<String, Object> getStatus(Long recipeId, Long userId) {
        permissionService.requireMemberOfRecipe(recipeId, userId);
        List<MenuItem> items = itemMapper.selectByRecipeId(recipeId);
        List<MenuSection> sections = sectionMapper.selectByRecipeId(recipeId);
        Map<Long, String> sectionNames = new HashMap<>();
        for (var sec : sections) {
            sectionNames.put(sec.getId(), sec.getName());
        }

        int total = items.size();
        long uploaded = items.stream().filter(i -> "uploaded".equals(i.getImageStatus())).count();
        long failed = items.stream().filter(i -> "failed".equals(i.getImageStatus())).count();
        long pending = total - uploaded - failed;

        List<Map<String, Object>> missing = new ArrayList<>();
        for (var item : items) {
            if (!"uploaded".equals(item.getImageStatus())) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("itemId", item.getId());
                m.put("itemName", item.getName());
                m.put("sectionName", sectionNames.getOrDefault(item.getSectionId(), ""));
                missing.add(m);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalItems", total);
        result.put("uploadedItems", (int) uploaded);
        result.put("pendingItems", (int) pending);
        result.put("failedItems", (int) failed);
        result.put("missingItems", missing);
        return result;
    }

    // ---- 内部方法 ----

    private byte[] toJpegBytes(BufferedImage image) throws IOException {
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgbImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(rgbImage, "jpg", output)) {
            throw new IOException("无法编码 JPEG 图片");
        }
        return output.toByteArray();
    }

    private String validateMagicBytes(BufferedInputStream bis) throws IOException {
        byte[] header = new byte[12];
        int read = bis.read(header);
        if (read < 4) throw new BusinessException(10006, "无法识别图片格式");

        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "jpg";
        }
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50
                && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return "png";
        }
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49
                && header[2] == (byte) 0x46 && header[3] == (byte) 0x46
                && header[8] == (byte) 0x57 && header[9] == (byte) 0x45
                && header[10] == (byte) 0x42 && header[11] == (byte) 0x50) {
            return "webp";
        }
        if (header[0] == (byte) 0x42 && header[1] == (byte) 0x4D) {
            return "bmp";
        }
        if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49
                && header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
            return "gif";
        }
        throw new BusinessException(10006, "仅支持 JPEG、PNG、WebP、BMP、GIF 格式");
    }
}
