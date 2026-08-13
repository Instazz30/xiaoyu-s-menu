package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.entity.*;
import com.xiaxiaoyu.xingbangmenu.mapper.*;
import com.xiaxiaoyu.xingbangmenu.template.PosterContext;
import com.xiaxiaoyu.xingbangmenu.template.PosterTemplate;
import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PosterGenerationTask {

    private static final Logger log = LoggerFactory.getLogger(PosterGenerationTask.class);

    private final MenuRecipeMapper recipeMapper;
    private final MenuSectionMapper sectionMapper;
    private final MenuItemMapper itemMapper;
    private final ImageAssetMapper imageMapper;
    private final MenuPosterMapper posterMapper;
    private final TemplateRegistry templateRegistry;
    private final OssStorageService storageService;
    private final GroupPosterTemplateService groupTemplateService;
    private final PublicPosterBackgroundService backgroundService;

    @Value("${upload.storage-path:./uploads}")
    private String legacyStoragePath;

    public PosterGenerationTask(MenuRecipeMapper recipeMapper,
                                 MenuSectionMapper sectionMapper,
                                 MenuItemMapper itemMapper,
                                 ImageAssetMapper imageMapper,
                                 MenuPosterMapper posterMapper,
                                 TemplateRegistry templateRegistry,
                                 OssStorageService storageService,
                                 GroupPosterTemplateService groupTemplateService,
                                 PublicPosterBackgroundService backgroundService) {
        this.recipeMapper = recipeMapper;
        this.sectionMapper = sectionMapper;
        this.itemMapper = itemMapper;
        this.imageMapper = imageMapper;
        this.posterMapper = posterMapper;
        this.templateRegistry = templateRegistry;
        this.storageService = storageService;
        this.groupTemplateService = groupTemplateService;
        this.backgroundService = backgroundService;
    }

    @Async("posterExecutor")
    public void execute(Long posterId, Long recipeId, String templateId) {
        MenuPoster poster = posterMapper.selectById(posterId);
        if (poster == null) return;

        try {
            MenuRecipe recipe = recipeMapper.selectById(recipeId);
            if (recipe == null) {
                fail(poster, "菜谱不存在");
                return;
            }

            GroupPosterTemplate customTemplate = groupTemplateService.getForGeneration(templateId, recipe.getGroupId());
            PosterTemplate template = selectTemplate(customTemplate != null
                    ? customTemplate.getBaseTemplateId() : templateId);
            if (template == null) {
                fail(poster, "未找到指定模板");
                return;
            }

            PosterContext ctx = buildContext(recipe);
            if (customTemplate != null) {
                if (customTemplate.getBackgroundUrl() != null) {
                    ctx.setCustomBackgroundPath(backgroundService.resolvePath(customTemplate.getBackgroundUrl()));
                }
                ctx.setLogoPath(resolvePath(customTemplate.getLogoUrl()));
                ctx.setLogoSlot(customTemplate.getLogoSlot());
                ctx.setQrCodePath(resolvePath(customTemplate.getQrCodeUrl()));
                ctx.setQrCodeSlot(customTemplate.getQrCodeSlot());
            }
            List<BufferedImage> pages = template.render(ctx);

            String baseName = UUID.randomUUID().toString();
            List<String> urls = new ArrayList<>(pages.size());

            for (int i = 0; i < pages.size(); i++) {
                String filename = baseName + "_page" + (i + 1) + ".jpg";
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!ImageIO.write(pages.get(i), "jpg", output)) {
                    throw new IllegalStateException("无法编码海报图片");
                }
                urls.add(storageService.upload(
                        "recipes/" + recipeId + "/posters/" + filename,
                        output.toByteArray(), "image/jpeg"));
            }

            poster.setGenerationStatus("completed");
            poster.setPageCount(pages.size());
            poster.setOutputUrls(String.join(",", urls));
            posterMapper.update(poster);

            recipe.setCurrentPosterId(posterId);
            recipe.setStatus("generated");
            recipeMapper.update(recipe);

            log.info("Poster generated: recipeId={}, posterId={}, pages={}", recipeId, posterId, pages.size());

        } catch (Exception e) {
            log.error("Poster generation failed: recipeId={}, posterId={}", recipeId, posterId, e);
            fail(poster, e.getMessage() != null ? e.getMessage() : "海报生成失败");
        }
    }

    private void fail(MenuPoster poster, String message) {
        poster.setGenerationStatus("failed");
        poster.setErrorMessage(message);
        posterMapper.update(poster);
    }

    private PosterTemplate selectTemplate(String templateId) {
        return templateRegistry.getById(templateId);
    }

    private PosterContext buildContext(MenuRecipe recipe) {
        PosterContext ctx = new PosterContext();
        ctx.setTitle(recipe.getTitle());
        ctx.setIssue(recipe.getIssue() != null ? recipe.getIssue() : 1);

        if (recipe.getRecipeDate() != null) {
            ctx.setDateText(formatDate(recipe.getRecipeDate()));
        }
        ctx.setShowDate(Boolean.TRUE.equals(recipe.getDisplayDate()));

        ctx.setCanteenName(recipe.getCanteenName());
        ctx.setShowCanteen(Boolean.TRUE.equals(recipe.getDisplayCanteen()));

        ctx.setShowPrice(recipe.getDisplayPrice() == null || recipe.getDisplayPrice());

        List<MenuSection> sections = sectionMapper.selectByRecipeId(recipe.getId());
        List<MenuItem> allItems = itemMapper.selectByRecipeId(recipe.getId());
        Map<Long, List<MenuItem>> itemsBySection = allItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getSectionId));

        Map<Long, ImageAsset> imageByItem = new HashMap<>();
        for (MenuItem item : allItems) {
            if (item.getImageId() != null) {
                ImageAsset img = imageMapper.selectById(item.getImageId());
                if (img != null) {
                    imageByItem.put(item.getId(), img);
                }
            }
        }

        List<PosterContext.SectionData> sectionDataList = new ArrayList<>();
        for (MenuSection sec : sections) {
            PosterContext.SectionData sd = new PosterContext.SectionData();
            sd.setName(sec.getName());
            sd.setPriceText(sec.getPriceText());
            sd.setXiaowan(Boolean.TRUE.equals(sec.getIsXiaowan()));

            List<MenuItem> secItems = itemsBySection.getOrDefault(sec.getId(), List.of());
            List<PosterContext.ItemData> itemDataList = new ArrayList<>();
            for (MenuItem item : secItems) {
                PosterContext.ItemData id = new PosterContext.ItemData();
                id.setName(item.getName());
                ImageAsset img = imageByItem.get(item.getId());
                if (img != null) {
                    // 新图片使用 OSS URL，历史图片仍解析为本地路径
                    id.setImagePath(resolvePath(img.getOriginalUrl()));
                }
                itemDataList.add(id);
            }
            sd.setItems(itemDataList);
            sectionDataList.add(sd);
        }

        // 小碗菜自由图（item_id 为空），按上传时间正序排列
        List<PosterContext.ItemData> xiaowanImages = imageMapper.selectByRecipeId(recipe.getId())
                .stream()
                .filter(img -> img.getItemId() == null)
                .filter(img -> "approved".equals(img.getReviewStatus()))
                .sorted(Comparator.comparing(ImageAsset::getCreatedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(img -> {
                    PosterContext.ItemData id = new PosterContext.ItemData();
                    id.setImagePath(resolvePath(img.getOriginalUrl()));
                    return id;
                })
                .collect(Collectors.toList());

        ctx.setSections(sectionDataList);
        ctx.setXiaowanImages(xiaowanImages);
        return ctx;
    }

    private String resolvePath(String url) {
        if (url == null) return null;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        // url like "/uploads/1/uuid.jpg" → absolute path
        String relative = url.startsWith("/uploads/") ? url.substring("/uploads/".length()) : url;
        return java.nio.file.Paths.get(legacyStoragePath, relative).toAbsolutePath().toString();
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
    }
}
