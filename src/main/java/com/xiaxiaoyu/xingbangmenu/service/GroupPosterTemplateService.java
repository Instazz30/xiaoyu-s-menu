package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.entity.GroupPosterTemplate;
import com.xiaxiaoyu.xingbangmenu.entity.SysGroup;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.GroupPosterTemplateMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.SysGroupMapper;
import com.xiaxiaoyu.xingbangmenu.template.PosterTemplate;
import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class GroupPosterTemplateService {

    public static final String CUSTOM_PREFIX = "group_";
    private static final int MAX_TEMPLATES = 10;
    private static final long MAX_ASSET_SIZE = 8L * 1024 * 1024;
    private static final Set<String> STATUSES = Set.of("draft", "published", "disabled");
    private static final Set<String> LOGO_SLOTS = Set.of("top_left", "top_right");
    private static final Set<String> QR_SLOTS = Set.of("top_right", "bottom_right");

    private final GroupPosterTemplateMapper templateMapper;
    private final SysGroupMapper groupMapper;
    private final PermissionService permissionService;
    private final TemplateRegistry templateRegistry;
    private final OssStorageService storageService;
    private final PublicPosterBackgroundService backgroundService;

    public GroupPosterTemplateService(GroupPosterTemplateMapper templateMapper,
                                      SysGroupMapper groupMapper,
                                      PermissionService permissionService,
                                      TemplateRegistry templateRegistry,
                                      OssStorageService storageService,
                                      PublicPosterBackgroundService backgroundService) {
        this.templateMapper = templateMapper;
        this.groupMapper = groupMapper;
        this.permissionService = permissionService;
        this.templateRegistry = templateRegistry;
        this.storageService = storageService;
        this.backgroundService = backgroundService;
    }

    public List<Map<String, Object>> listAvailable(Long groupId, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PosterTemplate template : templateRegistry.getAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", template.getId());
            item.put("name", template.getName());
            item.put("scope", "public");
            item.put("baseTemplateId", template.getId());
            item.put("canEdit", false);
            item.put("canCopy", true);
            item.put("status", "published");
            item.put("logoSlots", List.of("top_left"));
            item.put("qrCodeSlots", List.of("top_right"));
            result.add(item);
        }
        for (GroupPosterTemplate template : templateMapper.selectByGroupId(groupId)) {
            if ("published".equals(template.getStatus())) {
                result.add(toMap(template, userId));
            }
        }
        return result;
    }

    public List<Map<String, Object>> listGroupTemplates(Long groupId, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        return templateMapper.selectByGroupId(groupId).stream().map(t -> toMap(t, userId)).toList();
    }

    public GroupPosterTemplate getForGeneration(String templateId, Long groupId) {
        Long id = parseCustomId(templateId);
        if (id == null) return null;
        GroupPosterTemplate template = templateMapper.selectById(id);
        if (template == null || !groupId.equals(template.getGroupId()) || !"published".equals(template.getStatus())) {
            throw new BusinessException(400, "小组专属模板不存在或尚未发布");
        }
        return template;
    }

    @Transactional
    public Map<String, Object> create(Long groupId, Long userId, Map<String, Object> body) {
        permissionService.requireAdmin(groupId, userId);
        if (templateMapper.countByGroupId(groupId) >= MAX_TEMPLATES) {
            throw new BusinessException(400, "每个小组最多保存10个专属模板");
        }
        GroupPosterTemplate template = new GroupPosterTemplate();
        template.setGroupId(groupId);
        template.setCreatorId(userId);
        applyBody(template, body, true);
        templateMapper.insert(template);
        return toMap(templateMapper.selectById(template.getId()), userId);
    }

    @Transactional
    public Map<String, Object> update(Long groupId, Long id, Long userId, Map<String, Object> body) {
        GroupPosterTemplate template = requireEditable(groupId, id, userId);
        applyBody(template, body, false);
        templateMapper.update(template);
        return toMap(templateMapper.selectById(id), userId);
    }

    @Transactional
    public void setDefault(Long groupId, Long id, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        GroupPosterTemplate template = requireInGroup(groupId, id);
        if (!"published".equals(template.getStatus())) {
            throw new BusinessException(400, "只有已发布模板可以设为默认");
        }
        templateMapper.clearDefault(groupId);
        template.setIsDefault(true);
        templateMapper.update(template);
    }

    @Transactional
    public void delete(Long groupId, Long id, Long userId) {
        requireEditable(groupId, id, userId);
        templateMapper.deleteById(id);
    }

    public Map<String, String> uploadAsset(Long groupId, Long userId, String type, MultipartFile file) {
        permissionService.requireAdmin(groupId, userId);
        if (!Set.of("logo", "qrcode").contains(type)) {
            throw new BusinessException(400, "不支持的模板素材类型");
        }
        if (file == null || file.isEmpty() || file.getSize() > MAX_ASSET_SIZE) {
            throw new BusinessException(400, "图片不能为空且不能超过8MB");
        }
        try {
            byte[] bytes = file.getBytes();
            if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                throw new BusinessException(400, "请选择有效的图片文件");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) contentType = "image/jpeg";
            String ext = contentType.contains("png") ? ".png" : ".jpg";
            String url = storageService.upload("groups/" + groupId + "/poster-templates/" +
                    type + "_" + UUID.randomUUID() + ext, bytes, contentType);
            return Map.of("url", url);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "模板素材上传失败，请重试");
        }
    }

    public String customId(Long id) {
        return CUSTOM_PREFIX + id;
    }

    private void applyBody(GroupPosterTemplate template, Map<String, Object> body, boolean creating) {
        String name = string(body.get("name"));
        if (creating || name != null) {
            if (name == null || name.isBlank()) throw new BusinessException(400, "模板名称不能为空");
            template.setName(name.trim());
        }
        String baseId = string(body.get("baseTemplateId"));
        if (creating || baseId != null) {
            if (baseId == null || !templateRegistry.exists(baseId)) {
                throw new BusinessException(400, "请选择有效的公共基础模板");
            }
            template.setBaseTemplateId(baseId);
        }
        if (body.containsKey("backgroundUrl")) {
            String backgroundId = emptyToNull(string(body.get("backgroundUrl")));
            if (backgroundId != null && !backgroundService.exists(backgroundId)) {
                throw new BusinessException(400, "请选择有效的公共背景");
            }
            template.setBackgroundUrl(backgroundId);
        }
        if (body.containsKey("logoUrl")) template.setLogoUrl(emptyToNull(string(body.get("logoUrl"))));
        if (body.containsKey("qrCodeUrl")) template.setQrCodeUrl(emptyToNull(string(body.get("qrCodeUrl"))));

        String logoSlot = string(body.get("logoSlot"));
        if (logoSlot != null) {
            if (!LOGO_SLOTS.contains(logoSlot)) throw new BusinessException(400, "无效的Logo点位");
            template.setLogoSlot(logoSlot);
        } else if (creating) template.setLogoSlot("top_left");

        String qrSlot = string(body.get("qrCodeSlot"));
        if (qrSlot != null) {
            if (!QR_SLOTS.contains(qrSlot)) throw new BusinessException(400, "无效的二维码点位");
            template.setQrCodeSlot(qrSlot);
        } else if (creating) template.setQrCodeSlot("top_right");

        if (body.get("displayPrice") instanceof Boolean value) template.setDisplayPrice(value);
        else if (creating) template.setDisplayPrice(true);
        if (body.get("displayDate") instanceof Boolean value) template.setDisplayDate(value);
        else if (creating) template.setDisplayDate(true);
        if (body.get("displayCanteen") instanceof Boolean value) template.setDisplayCanteen(value);
        else if (creating) template.setDisplayCanteen(true);

        String status = string(body.get("status"));
        if (status != null) {
            if (!STATUSES.contains(status)) throw new BusinessException(400, "无效的模板状态");
            template.setStatus(status);
        } else if (creating) template.setStatus("draft");
        if (creating) template.setIsDefault(false);
        if ("published".equals(template.getStatus())
                && template.getBackgroundUrl() == null
                && template.getLogoUrl() == null
                && template.getQrCodeUrl() == null) {
            throw new BusinessException(400, "发布专属模板前请至少上传一项自定义素材");
        }
    }

    private GroupPosterTemplate requireEditable(Long groupId, Long id, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        GroupPosterTemplate template = requireInGroup(groupId, id);
        SysGroup group = groupMapper.selectById(groupId);
        if (!userId.equals(template.getCreatorId()) && (group == null || !userId.equals(group.getOwnerId()))) {
            throw new BusinessException(403, "只有模板创建者或小组所有者可以修改此模板");
        }
        return template;
    }

    private GroupPosterTemplate requireInGroup(Long groupId, Long id) {
        GroupPosterTemplate template = templateMapper.selectById(id);
        if (template == null || !groupId.equals(template.getGroupId())) {
            throw new BusinessException(404, "模板不存在");
        }
        return template;
    }

    private Map<String, Object> toMap(GroupPosterTemplate template, Long userId) {
        SysGroup group = groupMapper.selectById(template.getGroupId());
        boolean canEdit = userId.equals(template.getCreatorId()) ||
                (group != null && userId.equals(group.getOwnerId()));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", customId(template.getId()));
        item.put("customTemplateId", template.getId());
        item.put("groupId", template.getGroupId());
        item.put("creatorId", template.getCreatorId());
        item.put("name", template.getName());
        item.put("scope", "group");
        item.put("baseTemplateId", template.getBaseTemplateId());
        item.put("backgroundUrl", template.getBackgroundUrl());
        item.put("backgroundPreviewUrl", backgroundService.resolveThumbnailUrl(template.getBackgroundUrl()));
        item.put("logoUrl", template.getLogoUrl());
        item.put("logoSlot", "top_left");
        item.put("qrCodeUrl", template.getQrCodeUrl());
        item.put("qrCodeSlot", "top_right");
        item.put("displayPrice", template.getDisplayPrice());
        item.put("displayDate", template.getDisplayDate());
        item.put("displayCanteen", template.getDisplayCanteen());
        item.put("status", template.getStatus());
        item.put("isDefault", Boolean.TRUE.equals(template.getIsDefault()));
        item.put("canEdit", canEdit);
        item.put("canCopy", true);
        item.put("logoSlots", List.of("top_left"));
        item.put("qrCodeSlots", List.of("top_right"));
        item.put("updatedAt", template.getUpdatedAt());
        return item;
    }

    private Long parseCustomId(String id) {
        if (id == null || !id.startsWith(CUSTOM_PREFIX)) return null;
        try {
            return Long.parseLong(id.substring(CUSTOM_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new BusinessException(400, "无效的专属模板编号");
        }
    }

    private String string(Object value) { return value instanceof String s ? s : null; }
    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
