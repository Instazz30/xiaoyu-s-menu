package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.entity.MenuPoster;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import com.xiaxiaoyu.xingbangmenu.service.PosterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PosterController {

    private final PosterService posterService;
    private final PermissionService permissionService;

    public PosterController(PosterService posterService, PermissionService permissionService) {
        this.posterService = posterService;
        this.permissionService = permissionService;
    }

    /** 提交海报生成 */
    @PostMapping("/recipes/{id}/posters")
    public Result<Map<String, Long>> generate(HttpServletRequest request,
                                               @PathVariable Long id,
                                               @RequestParam(required = false) String templateId) {
        SysUser user = permissionService.currentUser(request);
        Long posterId = posterService.submitGeneration(id, templateId, user.getId());
        return Result.ok(Map.of("posterId", posterId));
    }

    /** 查询海报生成状态 */
    @GetMapping("/recipes/{id}/posters/{posterId}")
    public Result<MenuPoster> getStatus(HttpServletRequest request,
                                         @PathVariable Long id,
                                         @PathVariable Long posterId) {
        SysUser user = permissionService.currentUser(request);
        MenuPoster poster = posterService.getStatus(posterId, user.getId());
        return Result.ok(poster);
    }

    /** 查询菜谱的所有海报 */
    @GetMapping("/recipes/{id}/posters")
    public Result<List<MenuPoster>> list(HttpServletRequest request, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        List<MenuPoster> posters = posterService.list(id, user.getId());
        return Result.ok(posters);
    }

    /** 重新生成海报 */
    @PostMapping("/recipes/{id}/posters/regenerate")
    public Result<Map<String, Long>> regenerate(HttpServletRequest request,
                                                 @PathVariable Long id,
                                                 @RequestParam Long posterId) {
        SysUser user = permissionService.currentUser(request);
        Long newPosterId = posterService.regenerate(id, posterId, user.getId());
        return Result.ok(Map.of("posterId", newPosterId));
    }
}
