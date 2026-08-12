package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.common.PageResult;
import com.xiaxiaoyu.xingbangmenu.dto.ParseRequest;
import com.xiaxiaoyu.xingbangmenu.dto.ParseResult;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeCreateRequest;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeDetailResponse;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeUpdateRequest;
import com.xiaxiaoyu.xingbangmenu.entity.ImageAsset;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.ImageAssetMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuSectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final MenuRecipeMapper recipeMapper;
    private final MenuSectionMapper sectionMapper;
    private final MenuItemMapper itemMapper;
    private final ImageAssetMapper imageAssetMapper;
    private final RecipeParser recipeParser;
    private final PermissionService permissionService;

    public RecipeService(MenuRecipeMapper recipeMapper,
                         MenuSectionMapper sectionMapper,
                         MenuItemMapper itemMapper,
                         ImageAssetMapper imageAssetMapper,
                         RecipeParser recipeParser,
                         PermissionService permissionService) {
        this.recipeMapper = recipeMapper;
        this.sectionMapper = sectionMapper;
        this.itemMapper = itemMapper;
        this.imageAssetMapper = imageAssetMapper;
        this.recipeParser = recipeParser;
        this.permissionService = permissionService;
    }

    /** 新建菜谱（仅小组管理员） */
    @Transactional
    public RecipeDetailResponse create(RecipeCreateRequest request, Long userId) {
        permissionService.requireAdmin(request.getGroupId(), userId);
        MenuRecipe recipe = new MenuRecipe();
        recipe.setTitle(request.getTitle() != null ? request.getTitle() : "今日菜谱");
        recipe.setRecipeDate(request.getRecipeDate() != null ? request.getRecipeDate() : LocalDate.now());
        recipe.setIssue(request.getIssue() != null ? request.getIssue() : 1);
        recipe.setCanteenName(request.getCanteenName());
        recipe.setGroupId(request.getGroupId());
        recipe.setCreatorId(userId);
        recipeMapper.insert(recipe);
        return toDetail(recipe);
    }

    /** 菜谱详情（小组成员可看） */
    public RecipeDetailResponse getDetail(Long id, Long userId) {
        MenuRecipe recipe = permissionService.requireMemberOfRecipe(id, userId);
        return toDetail(recipe);
    }

    /** 小组内菜谱分页列表（小组成员可看） */
    public PageResult<RecipeDetailResponse> list(Long groupId, String status, LocalDate dateFrom,
                                                 LocalDate dateTo, String keyword,
                                                 int page, int pageSize, Long userId) {
        permissionService.requireMember(groupId, userId);
        int offset = (page - 1) * pageSize;
        List<MenuRecipe> recipes = recipeMapper.selectList(groupId, status, dateFrom, dateTo, keyword, offset, pageSize);
        long total = recipeMapper.countList(groupId, status, dateFrom, dateTo, keyword);
        List<RecipeDetailResponse> list = recipes.stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        return new PageResult<>(list, total, page, pageSize);
    }

    /** 更新菜谱（仅管理员） */
    @Transactional
    public RecipeDetailResponse update(Long id, RecipeUpdateRequest request, Long userId) {
        MenuRecipe recipe = permissionService.requireAdminOfRecipe(id, userId);
        if (request.getTitle() != null) recipe.setTitle(request.getTitle());
        if (request.getRecipeDate() != null) recipe.setRecipeDate(request.getRecipeDate());
        if (request.getIssue() != null) recipe.setIssue(request.getIssue());
        if (request.getCanteenName() != null) recipe.setCanteenName(request.getCanteenName());
        if (request.getTemplateId() != null) recipe.setTemplateId(request.getTemplateId());
        if (request.getDisplayPrice() != null) recipe.setDisplayPrice(request.getDisplayPrice());
        if (request.getDisplayDate() != null) recipe.setDisplayDate(request.getDisplayDate());
        if (request.getDisplayCanteen() != null) recipe.setDisplayCanteen(request.getDisplayCanteen());
        recipeMapper.update(recipe);
        return getDetail(id, userId);
    }

    /** 删除菜谱（仅管理员） */
    @Transactional
    public void delete(Long id, Long userId) {
        permissionService.requireAdminOfRecipe(id, userId);
        sectionMapper.deleteByRecipeId(id);
        itemMapper.deleteByRecipeId(id);
        recipeMapper.deleteById(id);
    }

    /** 复制菜谱（仅管理员） */
    @Transactional
    public RecipeDetailResponse copy(Long id, Long userId) {
        MenuRecipe source = permissionService.requireAdminOfRecipe(id, userId);
        MenuRecipe copy = new MenuRecipe();
        copy.setTitle(source.getTitle() + "（副本）");
        copy.setRecipeDate(source.getRecipeDate());
        copy.setIssue(source.getIssue());
        copy.setCanteenName(source.getCanteenName());
        copy.setGroupId(source.getGroupId());
        copy.setCreatorId(userId);
        copy.setTemplateId(source.getTemplateId());
        copy.setDisplayPrice(source.getDisplayPrice());
        copy.setDisplayDate(source.getDisplayDate());
        copy.setDisplayCanteen(source.getDisplayCanteen());
        recipeMapper.insert(copy);

        List<MenuSection> sourceSections = sectionMapper.selectByRecipeId(id);
        if (sourceSections.isEmpty()) {
            return toDetail(copy);
        }

        List<MenuSection> newSections = new ArrayList<>();
        List<MenuItem> allNewItems = new ArrayList<>();
        for (MenuSection sec : sourceSections) {
            MenuSection newSection = new MenuSection();
            newSection.setRecipeId(copy.getId());
            newSection.setName(sec.getName());
            newSection.setPrice(sec.getPrice());
            newSection.setPriceText(sec.getPriceText());
            newSection.setSortOrder(sec.getSortOrder());
            newSections.add(newSection);
        }
        sectionMapper.insertBatch(newSections);

        for (int i = 0; i < sourceSections.size(); i++) {
            Long newSectionId = newSections.get(i).getId();
            for (MenuItem item : itemMapper.selectBySectionId(sourceSections.get(i).getId())) {
                MenuItem newItem = new MenuItem();
                newItem.setSectionId(newSectionId);
                newItem.setRecipeId(copy.getId());
                newItem.setName(item.getName());
                newItem.setDescription(item.getDescription());
                newItem.setSortOrder(item.getSortOrder());
                newItem.setImageStatus("pending");
                allNewItems.add(newItem);
            }
        }
        if (!allNewItems.isEmpty()) {
            itemMapper.insertBatch(allNewItems);
        }
        return toDetail(copy);
    }

    /** 智能解析（仅管理员），解析生成菜品时自动写修改记录 */
    @Transactional
    public ParseResult parse(Long id, ParseRequest request, Long userId) {
        MenuRecipe recipe = permissionService.requireAdminOfRecipe(id, userId);
        recipe.setOriginalText(request.getOriginalText());
        recipeMapper.update(recipe);

        if ("draft".equals(recipe.getStatus())) {
            sectionMapper.deleteByRecipeId(id);
            itemMapper.deleteByRecipeId(id);
        }
        //管理员输入的文本菜单会被保存到originaltext当中
        ParseResult result = recipeParser.parse(request.getOriginalText());
        if (result.getSections() != null) {
            int sectionOrder = 0;
            for (ParseResult.SectionResult sr : result.getSections()) {
                MenuSection section = new MenuSection();
                section.setRecipeId(id);
                section.setName(sr.getName());
                section.setPrice(sr.getPrice());
                section.setPriceText(sr.getPriceText());
                section.setSortOrder(sectionOrder++);
                section.setNeedsConfirmation(sr.isNeedsConfirmation());
                section.setIsXiaowan(sr.isXiaowan());
                sectionMapper.insert(section);

                if (sr.getItems() != null) {
                    int itemOrder = 0;
                    for (String itemName : sr.getItems()) {
                        MenuItem item = new MenuItem();
                        item.setSectionId(section.getId());
                        item.setRecipeId(id);
                        item.setName(itemName);
                        item.setSortOrder(itemOrder++);
                        item.setNeedsConfirmation(sr.isNeedsConfirmation());
                        itemMapper.insert(item);
                    }
                }
            }
        }
        return result;
    }

    /** 确认解析（仅管理员） */
    @Transactional
    public RecipeDetailResponse confirm(Long id, Long userId) {
        MenuRecipe recipe = permissionService.requireAdminOfRecipe(id, userId);
        if (!"draft".equals(recipe.getStatus())) {
            throw new BusinessException(10002, "当前状态不允许确认，请重新解析");
        }
        List<MenuSection> sections = sectionMapper.selectByRecipeId(id);
        if (sections.isEmpty()) {
            throw new BusinessException(400, "请至少添加一个区域");
        }
        for (MenuSection sec : sections) {
            if (itemMapper.selectBySectionId(sec.getId()).isEmpty()) {
                throw new BusinessException(400, "区域\"" + sec.getName() + "\"下没有菜品");
            }
        }
        recipe.setStatus("pending_photo");
        recipeMapper.update(recipe);
        return toDetail(recipe);
    }

    /** 批量复用（仅管理员） */
    @Transactional
    public List<RecipeDetailResponse> applyBatch(Long id, List<LocalDate> dates, Long userId) {
        MenuRecipe source = permissionService.requireAdminOfRecipe(id, userId);
        if (dates == null || dates.isEmpty()) {
            throw new BusinessException(400, "请提供至少一个目标日期");
        }
        List<RecipeDetailResponse> results = new ArrayList<>();
        for (LocalDate date : dates) {
            RecipeDetailResponse copied = copy(id, userId);
            RecipeUpdateRequest updateReq = new RecipeUpdateRequest();
            updateReq.setRecipeDate(date);
            updateReq.setTitle(source.getTitle());
            update(copied.getId(), updateReq, userId);
            results.add(getDetail(copied.getId(), userId));
        }
        return results;
    }

    /** 设为小组当前菜单（仅管理员） */
    @Transactional
    public RecipeDetailResponse setCurrent(Long id, Long userId) {
        MenuRecipe recipe = permissionService.requireAdminOfRecipe(id, userId);
        recipeMapper.clearCurrentByGroup(recipe.getGroupId());
        recipe.setIsCurrent(true);
        recipeMapper.update(recipe);
        return toDetail(recipe);
    }

    /** 小组当前菜单（小组成员可看，无则返回 null） */
    public RecipeDetailResponse currentMenu(Long groupId, Long userId) {
        permissionService.requireMember(groupId, userId);
        MenuRecipe recipe = recipeMapper.selectCurrentByGroup(groupId);
        return recipe == null ? null : toDetail(recipe);
    }

    // ---- 内部方法 ----

    private RecipeDetailResponse toDetail(MenuRecipe recipe) {
        RecipeDetailResponse dto = new RecipeDetailResponse();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setRecipeDate(recipe.getRecipeDate());
        dto.setIssue(recipe.getIssue());
        dto.setCanteenName(recipe.getCanteenName());
        dto.setGroupId(recipe.getGroupId());
        dto.setIsCurrent(recipe.getIsCurrent());
        dto.setCreatorId(recipe.getCreatorId());
        dto.setOriginalText(recipe.getOriginalText());
        dto.setStatus(recipe.getStatus());
        dto.setTemplateId(recipe.getTemplateId());
        dto.setDisplayPrice(recipe.getDisplayPrice());
        dto.setDisplayDate(recipe.getDisplayDate());
        dto.setDisplayCanteen(recipe.getDisplayCanteen());
        dto.setCurrentPosterId(recipe.getCurrentPosterId());
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());

        List<MenuSection> sections = sectionMapper.selectByRecipeId(recipe.getId());
        List<MenuItem> allItems = itemMapper.selectByRecipeId(recipe.getId());
        Map<Long, List<MenuItem>> itemsBySection = allItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getSectionId));

        List<ImageAsset> imageAssets = imageAssetMapper.selectByRecipeId(recipe.getId());
        Map<Long, ImageAsset> imageMap = imageAssets.stream()
                .collect(Collectors.toMap(ImageAsset::getId, img -> img, (a, b) -> a));
        Map<Long, List<ImageAsset>> imagesByItem = imageAssets.stream()
                .filter(img -> img.getItemId() != null)
                .collect(Collectors.groupingBy(ImageAsset::getItemId));

        List<RecipeDetailResponse.SectionDto> sectionDtos = new ArrayList<>();
        for (MenuSection sec : sections) {
            RecipeDetailResponse.SectionDto sd = new RecipeDetailResponse.SectionDto();
            sd.setId(sec.getId());
            sd.setRecipeId(sec.getRecipeId());
            sd.setName(sec.getName());
            sd.setPrice(sec.getPrice());
            sd.setPriceText(sec.getPriceText());
            sd.setSortOrder(sec.getSortOrder());
            sd.setNeedsConfirmation(sec.getNeedsConfirmation());
            sd.setIsXiaowan(sec.getIsXiaowan());

            List<MenuItem> secItems = itemsBySection.getOrDefault(sec.getId(), List.of());
            List<RecipeDetailResponse.ItemDto> itemDtos = secItems.stream().map(item -> {
                RecipeDetailResponse.ItemDto idto = new RecipeDetailResponse.ItemDto();
                idto.setId(item.getId());
                idto.setSectionId(item.getSectionId());
                idto.setRecipeId(item.getRecipeId());
                idto.setName(item.getName());
                idto.setDescription(item.getDescription());
                idto.setSortOrder(item.getSortOrder());
                idto.setImageId(item.getImageId());
                idto.setImageStatus(item.getImageStatus());
                idto.setNeedsConfirmation(item.getNeedsConfirmation());
                idto.setImages(imagesByItem.getOrDefault(item.getId(), List.of()));
                if (item.getImageId() != null) {
                    ImageAsset img = imageMap.get(item.getImageId());
                    if (img != null) {
                        idto.setImageUrl(img.getOriginalUrl());
                        idto.setThumbnailUrl(img.getThumbnailUrl());
                    }
                }
                return idto;
            }).collect(Collectors.toList());

            sd.setItems(itemDtos);
            sectionDtos.add(sd);
        }

        dto.setSections(sectionDtos);
        dto.setXiaowanImages(imageAssets.stream()
                .filter(img -> img.getItemId() == null)
                .sorted(java.util.Comparator.comparing(ImageAsset::getCreatedAt,
                        java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
                .collect(Collectors.toList()));
        return dto;
    }
}
