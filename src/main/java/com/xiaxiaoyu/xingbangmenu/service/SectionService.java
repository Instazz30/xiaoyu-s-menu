package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.dto.SectionRequest;
import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuSectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionService {

    private final MenuSectionMapper sectionMapper;
    private final MenuItemMapper itemMapper;
    private final PermissionService permissionService;

    public SectionService(MenuSectionMapper sectionMapper,
                          MenuItemMapper itemMapper,
                          PermissionService permissionService) {
        this.sectionMapper = sectionMapper;
        this.itemMapper = itemMapper;
        this.permissionService = permissionService;
    }

    @Transactional
    public MenuSection add(Long recipeId, SectionRequest request, Long userId) {
        permissionService.requireAdminOfRecipe(recipeId, userId);
        MenuSection section = new MenuSection();
        section.setRecipeId(recipeId);
        section.setName(request.getName().trim());
        section.setPriceText(request.getPriceText());
        section.setPrice(request.getPrice());
        section.setSortOrder(request.getSortOrder() != null ? request.getSortOrder()
                : sectionMapper.selectByRecipeId(recipeId).size());
        sectionMapper.insert(section);
        return section;
    }

    @Transactional
    public MenuSection update(Long recipeId, Long sectionId, SectionRequest request, Long userId) {
        permissionService.requireAdminOfRecipe(recipeId, userId);
        MenuSection section = sectionMapper.selectById(sectionId);
        if (section == null) {
            throw new BusinessException(10003, "区域不存在或已删除");
        }
        if (request.getName() != null) section.setName(request.getName().trim());
        if (request.getPriceText() != null) section.setPriceText(request.getPriceText());
        if (request.getPrice() != null) section.setPrice(request.getPrice());
        if (request.getSortOrder() != null) section.setSortOrder(request.getSortOrder());
        sectionMapper.update(section);
        return sectionMapper.selectById(sectionId);
    }

    @Transactional
    public void delete(Long recipeId, Long sectionId, Long userId) {
        permissionService.requireAdminOfRecipe(recipeId, userId);
        MenuSection section = sectionMapper.selectById(sectionId);
        if (section == null) {
            throw new BusinessException(10003, "区域不存在或已删除");
        }
        itemMapper.deleteBySectionId(sectionId);
        sectionMapper.deleteById(sectionId);
    }

    public List<MenuSection> list(Long recipeId, Long userId) {
        permissionService.requireMemberOfRecipe(recipeId, userId);
        return sectionMapper.selectByRecipeId(recipeId);
    }
}
