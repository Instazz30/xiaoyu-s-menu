package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.dto.ItemRequest;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuSectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ItemService {

    private final MenuRecipeMapper recipeMapper;
    private final MenuSectionMapper sectionMapper;
    private final MenuItemMapper itemMapper;
    private final PermissionService permissionService;
    private final MenuLogService menuLogService;

    public ItemService(MenuRecipeMapper recipeMapper,
                       MenuSectionMapper sectionMapper,
                       MenuItemMapper itemMapper,
                       PermissionService permissionService,
                       MenuLogService menuLogService) {
        this.recipeMapper = recipeMapper;
        this.sectionMapper = sectionMapper;
        this.itemMapper = itemMapper;
        this.permissionService = permissionService;
        this.menuLogService = menuLogService;
    }

    /** 添加菜品（小组成员均可，属于“上传之后”的变更，写入修改记录） */
    @Transactional
    public MenuItem add(Long recipeId, ItemRequest request, Long userId) {
        MenuRecipe recipe = permissionService.requireMemberOfRecipe(recipeId, userId);
        Long sectionId = request.getSectionId();
        if (sectionId == null) {
            throw new BusinessException("请指定所属区域");
        }
        MenuSection section = sectionMapper.selectById(sectionId);
        if (section == null) {
            throw new BusinessException(10003, "区域不存在或已删除");
        }
        MenuItem item = new MenuItem();
        item.setSectionId(sectionId);
        item.setRecipeId(recipeId);
        item.setName(request.getName().trim());
        item.setDescription(request.getDescription());
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder()
                : itemMapper.selectBySectionId(sectionId).size());
        item.setImageStatus("pending");
        itemMapper.insert(item);
        // 菜单上传之后的“新增菜品”属于后续变更，写入修改记录
        menuLogService.record(recipe, item, userId, "create", null, null, item.getName());
        return item;
    }

    /** 更新菜品（管理员与普通成员均可），自动生成修改记录 */
    @Transactional
    public MenuItem update(Long recipeId, Long itemId, ItemRequest request, Long userId) {
        MenuRecipe recipe = permissionService.requireMemberOfRecipe(recipeId, userId);
        MenuItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(10004, "菜品不存在或已删除");
        }

        if (request.getName() != null && !Objects.equals(item.getName(), request.getName().trim())) {
            String oldName = item.getName();
            item.setName(request.getName().trim());
            menuLogService.record(recipe, item, userId, "update", "name", oldName, item.getName());
        }
        if (request.getDescription() != null
                && !Objects.equals(item.getDescription(), request.getDescription())) {
            String oldDesc = item.getDescription();
            item.setDescription(request.getDescription());
            menuLogService.record(recipe, item, userId, "update", "description",
                    oldDesc == null ? "" : oldDesc, request.getDescription());
        }
        if (request.getSectionId() != null && !Objects.equals(item.getSectionId(), request.getSectionId())) {
            MenuSection target = sectionMapper.selectById(request.getSectionId());
            if (target == null) {
                throw new BusinessException(10003, "目标区域不存在或已删除");
            }
            MenuSection oldSection = sectionMapper.selectById(item.getSectionId());
            item.setSectionId(request.getSectionId());
            menuLogService.record(recipe, item, userId, "update", "section",
                    oldSection == null ? "" : oldSection.getName(),
                    target.getName());
        }
        if (request.getSortOrder() != null && !Objects.equals(item.getSortOrder(), request.getSortOrder())) {
            String oldSort = String.valueOf(item.getSortOrder());
            item.setSortOrder(request.getSortOrder());
            menuLogService.record(recipe, item, userId, "update", "sortOrder",
                    oldSort, String.valueOf(request.getSortOrder()));
        }
        itemMapper.update(item);
        return itemMapper.selectById(itemId);
    }

    /** 删除菜品（小组成员均可），删除行为写入修改记录 */
    @Transactional
    public void delete(Long recipeId, Long itemId, Long userId) {
        MenuRecipe recipe = permissionService.requireMemberOfRecipe(recipeId, userId);
        MenuItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(10004, "菜品不存在或已删除");
        }
        menuLogService.record(recipe, item, userId, "delete", null, item.getName(), null);
        itemMapper.deleteById(itemId);
    }

    public List<MenuItem> list(Long recipeId, Long userId) {
        permissionService.requireMemberOfRecipe(recipeId, userId);
        return itemMapper.selectByRecipeId(recipeId);
    }
}
