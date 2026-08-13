package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.entity.MenuPoster;
import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuPosterMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuSectionMapper;
import org.springframework.stereotype.Service;
import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateRegistry;

import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PosterService {

    private final MenuRecipeMapper recipeMapper;
    private final MenuPosterMapper posterMapper;
    private final MenuItemMapper itemMapper;
    private final MenuSectionMapper sectionMapper;
    private final PosterGenerationTask generationTask;
    private final PermissionService permissionService;
    private final GroupPosterTemplateService groupTemplateService;
    private final TemplateRegistry templateRegistry;

    public PosterService(MenuRecipeMapper recipeMapper,
                          MenuPosterMapper posterMapper,
                          MenuItemMapper itemMapper,
                          MenuSectionMapper sectionMapper,
                          PosterGenerationTask generationTask,
                          PermissionService permissionService,
                          GroupPosterTemplateService groupTemplateService,
                          TemplateRegistry templateRegistry) {
        this.recipeMapper = recipeMapper;
        this.posterMapper = posterMapper;
        this.itemMapper = itemMapper;
        this.sectionMapper = sectionMapper;
        this.generationTask = generationTask;
        this.permissionService = permissionService;
        this.groupTemplateService = groupTemplateService;
        this.templateRegistry = templateRegistry;
    }

    public Long submitGeneration(Long recipeId, String templateId, Long userId) {
        MenuRecipe recipe = permissionService.requireAdminOfRecipe(recipeId, userId);

        String selectedTemplateId = templateId != null ? templateId : recipe.getTemplateId();
        if (selectedTemplateId == null) {
            selectedTemplateId = templateRegistry.getAll().isEmpty() ? null : templateRegistry.getAll().get(0).getId();
        }
        if (selectedTemplateId == null) throw new BusinessException(400, "暂无可用海报模板");
        if (selectedTemplateId.startsWith(GroupPosterTemplateService.CUSTOM_PREFIX)) {
            groupTemplateService.getForGeneration(selectedTemplateId, recipe.getGroupId());
        } else if (!templateRegistry.exists(selectedTemplateId)) {
            throw new BusinessException(400, "海报模板不存在");
        }

        if (!"draft".equals(recipe.getStatus())
                && !"pending_photo".equals(recipe.getStatus())
                && !"photos_ready".equals(recipe.getStatus())
                && !"generated".equals(recipe.getStatus())) {
            throw new BusinessException(10002, "当前状态不允许生成海报");
        }

        // 校验所有“套餐区”菜品均已绑定审核通过的图片（小碗菜区域使用自由图，不逐菜绑定）
        Set<Long> xiaowanSectionIds = sectionMapper.selectByRecipeId(recipeId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsXiaowan()))
                .map(MenuSection::getId)
                .collect(Collectors.toSet());
        List<MenuItem> items = itemMapper.selectByRecipeId(recipeId);
        List<String> missing = items.stream()
                .filter(i -> !xiaowanSectionIds.contains(i.getSectionId()))
                .filter(i -> i.getImageId() == null)
                .map(MenuItem::getName)
                .toList();
        if (!missing.isEmpty()) {
            throw new BusinessException(400, "以下菜品还没有审核通过的图片: " + String.join("、", missing));
        }

        MenuPoster poster = new MenuPoster();
        poster.setRecipeId(recipeId);
        poster.setGroupId(recipe.getGroupId());
        poster.setCreatorId(userId);
        poster.setTemplateId(selectedTemplateId);
        poster.setGenerationStatus("processing");
        posterMapper.insert(poster);

        // 异步调用放在事务外 — INSERT 已持久化，异步线程可以查到这条记录
        generationTask.execute(poster.getId(), recipeId, poster.getTemplateId());

        return poster.getId();
    }

    public MenuPoster getStatus(Long posterId, Long userId) {
        MenuPoster poster = posterMapper.selectById(posterId);
        if (poster == null) {
            throw new BusinessException(10001, "海报记录不存在");
        }
        MenuRecipe recipe = recipeMapper.selectById(poster.getRecipeId());
        if (recipe != null) {
            permissionService.requireMember(recipe.getGroupId(), userId);
        }
        return poster;
    }

    public List<MenuPoster> list(Long recipeId, Long userId) {
        permissionService.requireMemberOfRecipe(recipeId, userId);
        return posterMapper.selectByRecipeId(recipeId);
    }

    public Long regenerate(Long recipeId, Long posterId, Long userId) {
        MenuPoster oldPoster = posterMapper.selectById(posterId);
        if (oldPoster == null) {
            throw new BusinessException(10001, "海报记录不存在");
        }
        return submitGeneration(recipeId, oldPoster.getTemplateId(), userId);
    }
}
