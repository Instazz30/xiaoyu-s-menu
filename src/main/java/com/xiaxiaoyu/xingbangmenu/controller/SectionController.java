package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.dto.SectionRequest;
import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import com.xiaxiaoyu.xingbangmenu.service.SectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes/{recipeId}/sections")
public class SectionController {

    private final SectionService sectionService;
    private final PermissionService permissionService;

    public SectionController(SectionService sectionService, PermissionService permissionService) {
        this.sectionService = sectionService;
        this.permissionService = permissionService;
    }

    /** 获取区域列表 */
    @GetMapping
    public Result<List<MenuSection>> list(HttpServletRequest request, @PathVariable Long recipeId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(sectionService.list(recipeId, user.getId()));
    }

    /** 添加区域 */
    @PostMapping
    public Result<MenuSection> add(HttpServletRequest request,
                                   @PathVariable Long recipeId,
                                   @Valid @RequestBody SectionRequest body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(sectionService.add(recipeId, body, user.getId()));
    }

    /** 更新区域 */
    @PutMapping("/{sectionId}")
    public Result<MenuSection> update(HttpServletRequest request,
                                      @PathVariable Long recipeId,
                                      @PathVariable Long sectionId,
                                      @Valid @RequestBody SectionRequest body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(sectionService.update(recipeId, sectionId, body, user.getId()));
    }

    /** 删除区域（级联删除菜品） */
    @DeleteMapping("/{sectionId}")
    public Result<Void> delete(HttpServletRequest request,
                               @PathVariable Long recipeId,
                               @PathVariable Long sectionId) {
        SysUser user = permissionService.currentUser(request);
        sectionService.delete(recipeId, sectionId, user.getId());
        return Result.ok();
    }
}
