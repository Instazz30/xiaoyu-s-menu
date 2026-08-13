package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.dto.ItemRequest;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.ItemService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes/{recipeId}/items")
public class ItemController {

    private final ItemService itemService;
    private final PermissionService permissionService;

    public ItemController(ItemService itemService, PermissionService permissionService) {
        this.itemService = itemService;
        this.permissionService = permissionService;
    }

    /** 获取菜品列表 */
    @GetMapping
    public Result<List<MenuItem>> list(HttpServletRequest request, @PathVariable Long recipeId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(itemService.list(recipeId, user.getId()));
    }

    /** 添加菜品 */
    @PostMapping
    public Result<MenuItem> add(HttpServletRequest request,
                                @PathVariable Long recipeId,
                                @Valid @RequestBody ItemRequest body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(itemService.add(recipeId, body, user.getId()));
    }

    /** 更新菜品（含移动区域） */
    @PutMapping("/{itemId}")
    public Result<MenuItem> update(HttpServletRequest request,
                                   @PathVariable Long recipeId,
                                   @PathVariable Long itemId,
                                   @Valid @RequestBody ItemRequest body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(itemService.update(recipeId, itemId, body, user.getId()));
    }

    /** 删除菜品 */
    @DeleteMapping("/{itemId}")
    public Result<Void> delete(HttpServletRequest request,
                               @PathVariable Long recipeId,
                               @PathVariable Long itemId) {
        SysUser user = permissionService.currentUser(request);
        itemService.delete(recipeId, itemId, user.getId());
        return Result.ok();
    }
}
