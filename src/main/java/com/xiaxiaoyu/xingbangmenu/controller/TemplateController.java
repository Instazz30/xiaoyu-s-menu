package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.GroupPosterTemplateService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final GroupPosterTemplateService groupTemplateService;
    private final PermissionService permissionService;

    public TemplateController(GroupPosterTemplateService groupTemplateService,
                              PermissionService permissionService) {
        this.groupTemplateService = groupTemplateService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list(HttpServletRequest request,
                                                  @org.springframework.web.bind.annotation.RequestParam Long groupId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(groupTemplateService.listAvailable(groupId, user.getId()));
    }
}
