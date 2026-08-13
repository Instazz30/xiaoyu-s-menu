package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.PageResult;
import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeDetailResponse;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.GroupService;
import com.xiaxiaoyu.xingbangmenu.service.MenuLogService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import com.xiaxiaoyu.xingbangmenu.service.RecipeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;
    private final RecipeService recipeService;
    private final MenuLogService menuLogService;
    private final PermissionService permissionService;

    public GroupController(GroupService groupService,
                           RecipeService recipeService,
                           MenuLogService menuLogService,
                           PermissionService permissionService) {
        this.groupService = groupService;
        this.recipeService = recipeService;
        this.menuLogService = menuLogService;
        this.permissionService = permissionService;
    }

    /** 创建小组 */
    @PostMapping
    public Result<Map<String, Object>> create(HttpServletRequest request,
                                              @RequestBody Map<String, String> body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(groupService.create(user.getId(), body.get("name")));
    }

    /** 我加入的小组列表 */
    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> mine(HttpServletRequest request) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(groupService.myGroups(user.getId()));
    }

    /** 通过小组码加入 */
    @PostMapping("/join")
    public Result<Map<String, Object>> join(HttpServletRequest request,
                                            @RequestBody Map<String, String> body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(groupService.join(user.getId(), body.get("code")));
    }

    /** 小组详情（含成员列表；管理员可见小组码） */
    @GetMapping("/{groupId}")
    public Result<Map<String, Object>> detail(HttpServletRequest request,
                                              @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(groupService.detail(groupId, user.getId()));
    }

    /** 修改小组名称（管理员） */
    @PutMapping("/{groupId}")
    public Result<Void> update(HttpServletRequest request,
                               @PathVariable Long groupId,
                               @RequestBody Map<String, String> body) {
        SysUser user = permissionService.currentUser(request);
        groupService.updateName(groupId, user.getId(), body.get("name"));
        return Result.ok();
    }

    /** 移除成员（管理员） */
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(HttpServletRequest request,
                                     @PathVariable Long groupId,
                                     @PathVariable Long userId) {
        SysUser user = permissionService.currentUser(request);
        groupService.removeMember(groupId, user.getId(), userId);
        return Result.ok();
    }

    /** 退出小组 */
    @PostMapping("/{groupId}/leave")
    public Result<Void> leave(HttpServletRequest request, @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        groupService.leave(groupId, user.getId());
        return Result.ok();
    }

    /** 解散小组（管理员） */
    @DeleteMapping("/{groupId}")
    public Result<Void> dissolve(HttpServletRequest request, @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        groupService.dissolve(groupId, user.getId());
        return Result.ok();
    }

    /** 设为管理员（管理员） */
    @PostMapping("/{groupId}/admins")
    public Result<Void> addAdmin(HttpServletRequest request,
                                 @PathVariable Long groupId,
                                 @RequestBody Map<String, Long> body) {
        SysUser user = permissionService.currentUser(request);
        groupService.addAdmin(groupId, user.getId(), body.get("userId"));
        return Result.ok();
    }

    /** 转让管理员（管理员） */
    @PostMapping("/{groupId}/transfer")
    public Result<Void> transfer(HttpServletRequest request,
                                 @PathVariable Long groupId,
                                 @RequestBody Map<String, Long> body) {
        SysUser user = permissionService.currentUser(request);
        groupService.transfer(groupId, user.getId(), body.get("userId"));
        return Result.ok();
    }

    /** 当前小组菜单 */
    @GetMapping("/{groupId}/current-menu")
    public Result<RecipeDetailResponse> currentMenu(HttpServletRequest request,
                                                    @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(recipeService.currentMenu(groupId, user.getId()));
    }

    /** 小组历史菜单（分页 + 筛选） */
    @GetMapping("/{groupId}/menus")
    public Result<PageResult<RecipeDetailResponse>> menus(HttpServletRequest request,
                                                          @PathVariable Long groupId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "20") int pageSize,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                                          @RequestParam(required = false) String keyword) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(recipeService.list(groupId, status, dateFrom, dateTo, keyword, page, pageSize, user.getId()));
    }

    /** 小组修改记录（分页 + 筛选） */
    @GetMapping("/{groupId}/logs")
    public Result<PageResult<Map<String, Object>>> logs(HttpServletRequest request,
                                                        @PathVariable Long groupId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate menuDate,
                                                        @RequestParam(required = false) Long operatorId,
                                                        @RequestParam(required = false) String keyword) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(menuLogService.list(groupId, menuDate, operatorId, keyword, page, pageSize, user.getId()));
    }

    /** 导出修改记录 xlsx（仅管理员，按日期区间） */
    @GetMapping("/{groupId}/logs/export")
    public ResponseEntity<byte[]> exportLogs(HttpServletRequest request,
                                             @PathVariable Long groupId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        SysUser user = permissionService.currentUser(request);
        byte[] data = menuLogService.exportXlsx(groupId, dateFrom, dateTo, user.getId());
        String groupName = groupService.groupName(groupId);
        String filename = URLEncoder.encode(
                groupName + "_" + dateFrom + "_" + dateTo + ".xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    /** 申请相册上传权限（成员） */
    @PostMapping("/{groupId}/album-apply")
    public Result<Void> applyAlbum(HttpServletRequest request, @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        groupService.applyAlbumPermission(groupId, user.getId());
        return Result.ok();
    }

    /** 相册权限待审列表（管理员） */
    @GetMapping("/{groupId}/album-applications")
    public Result<List<Map<String, Object>>> albumApplications(HttpServletRequest request,
                                                               @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(groupService.albumApplications(groupId, user.getId()));
    }

    /** 通过相册权限申请（管理员） */
    @PostMapping("/{groupId}/album-applications/{userId}/approve")
    public Result<Void> approveAlbum(HttpServletRequest request,
                                     @PathVariable Long groupId,
                                     @PathVariable Long userId) {
        SysUser user = permissionService.currentUser(request);
        groupService.reviewAlbumPermission(groupId, user.getId(), userId, true);
        return Result.ok();
    }

    /** 拒绝相册权限申请（管理员） */
    @PostMapping("/{groupId}/album-applications/{userId}/reject")
    public Result<Void> rejectAlbum(HttpServletRequest request,
                                    @PathVariable Long groupId,
                                    @PathVariable Long userId) {
        SysUser user = permissionService.currentUser(request);
        groupService.reviewAlbumPermission(groupId, user.getId(), userId, false);
        return Result.ok();
    }

    /** 管理员直接开通成员的相册上传权限 */
    @PostMapping("/{groupId}/members/{userId}/album-grant")
    public Result<Void> grantAlbum(HttpServletRequest request,
                                   @PathVariable Long groupId,
                                   @PathVariable Long userId) {
        SysUser user = permissionService.currentUser(request);
        groupService.reviewAlbumPermission(groupId, user.getId(), userId, true);
        return Result.ok();
    }

    /** 管理员取消成员的相册上传权限 */
    @PostMapping("/{groupId}/members/{userId}/album-revoke")
    public Result<Void> revokeAlbum(HttpServletRequest request,
                                    @PathVariable Long groupId,
                                    @PathVariable Long userId) {
        SysUser user = permissionService.currentUser(request);
        groupService.reviewAlbumPermission(groupId, user.getId(), userId, false);
        return Result.ok();
    }
}
