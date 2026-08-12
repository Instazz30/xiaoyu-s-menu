package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.GroupPosterTemplateService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/poster-templates")
public class GroupPosterTemplateController {

    private final GroupPosterTemplateService templateService;
    private final PermissionService permissionService;

    public GroupPosterTemplateController(GroupPosterTemplateService templateService,
                                         PermissionService permissionService) {
        this.templateService = templateService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(HttpServletRequest request, @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(templateService.listGroupTemplates(groupId, user.getId()));
    }

    @PostMapping
    public Result<Map<String, Object>> create(HttpServletRequest request, @PathVariable Long groupId,
                                               @RequestBody Map<String, Object> body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(templateService.create(groupId, user.getId(), body));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(HttpServletRequest request, @PathVariable Long groupId,
                                               @PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(templateService.update(groupId, id, user.getId(), body));
    }

    @PostMapping("/{id}/default")
    public Result<Void> setDefault(HttpServletRequest request, @PathVariable Long groupId,
                                   @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        templateService.setDefault(groupId, id, user.getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long groupId, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        templateService.delete(groupId, id, user.getId());
        return Result.ok();
    }

    @PostMapping("/assets/{type}")
    public Result<Map<String, String>> uploadAsset(HttpServletRequest request, @PathVariable Long groupId,
                                                   @PathVariable String type,
                                                   @RequestPart("file") MultipartFile file) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(templateService.uploadAsset(groupId, user.getId(), type, file));
    }
}
