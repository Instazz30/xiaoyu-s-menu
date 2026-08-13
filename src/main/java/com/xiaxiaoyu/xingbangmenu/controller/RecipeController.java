package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.PageResult;
import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.dto.ParseRequest;
import com.xiaxiaoyu.xingbangmenu.dto.ParseResult;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeCreateRequest;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeDetailResponse;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeUpdateRequest;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import com.xiaxiaoyu.xingbangmenu.service.RecipeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final PermissionService permissionService;

    public RecipeController(RecipeService recipeService, PermissionService permissionService) {
        this.recipeService = recipeService;
        this.permissionService = permissionService;
    }

    /** 新建菜谱 */
    @PostMapping
    public Result<RecipeDetailResponse> create(HttpServletRequest request,
                                                @Valid @RequestBody RecipeCreateRequest body) {
        //检验当前用户登录信息
        SysUser user = permissionService.currentUser(request);
        RecipeDetailResponse result = recipeService.create(body, user.getId());
        return Result.ok(result);
    }

    /** 查询菜谱详情（含区域和菜品） */
    @GetMapping("/{id}")
    public Result<RecipeDetailResponse> getDetail(HttpServletRequest request, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        RecipeDetailResponse result = recipeService.getDetail(id, user.getId());
        return Result.ok(result);
    }

    /** 分页查询菜谱列表 */
    @GetMapping
    public Result<PageResult<RecipeDetailResponse>> list(
            HttpServletRequest request,
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String keyword) {
        SysUser user = permissionService.currentUser(request);
        PageResult<RecipeDetailResponse> result =
                recipeService.list(groupId, status, dateFrom, dateTo, keyword, page, pageSize, user.getId());
        return Result.ok(result);
    }

    /** 更新菜谱 */
    @PutMapping("/{id}")
    public Result<RecipeDetailResponse> update(HttpServletRequest request,
                                                @PathVariable Long id,
                                                @Valid @RequestBody RecipeUpdateRequest body) {
        SysUser user = permissionService.currentUser(request);
        RecipeDetailResponse result = recipeService.update(id, body, user.getId());
        return Result.ok(result);
    }

    /** 删除菜谱 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        recipeService.delete(id, user.getId());
        return Result.ok();
    }

    /** 复用菜谱 */
    @PostMapping("/{id}/copy")
    public Result<RecipeDetailResponse> copy(HttpServletRequest request, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        RecipeDetailResponse result = recipeService.copy(id, user.getId());
        return Result.ok(result);
    }

    /** 解析菜谱文本 */
    @PostMapping("/{id}/parse")
    public Result<ParseResult> parse(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @Valid @RequestBody ParseRequest body) {
        SysUser user = permissionService.currentUser(request);
        ParseResult result = recipeService.parse(id, body, user.getId());
        return Result.ok(result);
    }

    /** 确认解析结果，进入待拍照状态 */
    @PostMapping("/{id}/confirm")
    public Result<RecipeDetailResponse> confirm(HttpServletRequest request, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        RecipeDetailResponse result = recipeService.confirm(id, user.getId());
        return Result.ok(result);
    }

    /** 设为小组当前菜单（仅管理员） */
    @PostMapping("/{id}/set-current")
    public Result<RecipeDetailResponse> setCurrent(HttpServletRequest request, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(recipeService.setCurrent(id, user.getId()));
    }

    /** 批量复用菜谱到多个日期（周菜谱循环） */
    @PostMapping("/{id}/apply-batch")
    public Result<List<RecipeDetailResponse>> applyBatch(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> body) {
        List<String> dateStrs = body.get("dates");
        if (dateStrs == null || dateStrs.isEmpty()) {
            return Result.fail(400, "请提供至少一个目标日期");
        }
        List<LocalDate> dates = dateStrs.stream()
                .map(LocalDate::parse)
                .toList();
        SysUser user = permissionService.currentUser(request);
        List<RecipeDetailResponse> results = recipeService.applyBatch(id, dates, user.getId());
        return Result.ok(results);
    }
}
