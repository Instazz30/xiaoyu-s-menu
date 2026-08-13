package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import com.xiaxiaoyu.xingbangmenu.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final PermissionService permissionService;

    public StatisticsController(StatisticsService statisticsService, PermissionService permissionService) {
        this.statisticsService = statisticsService;
        this.permissionService = permissionService;
    }

    /** 统计概览 — 总菜谱数、按状态分布 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(HttpServletRequest request, @RequestParam Long groupId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(statisticsService.overview(groupId, user.getId()));
    }

    /** 月度趋势 — 近 N 个月每月菜谱数 */
    @GetMapping("/trends")
    public Result<List<Map<String, Object>>> trends(
            HttpServletRequest request,
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "6") int months) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(statisticsService.trends(groupId, months, user.getId()));
    }

    /** 热门菜品 — 出现次数最多的菜品 */
    @GetMapping("/popular-dishes")
    public Result<List<Map<String, Object>>> popularDishes(
            HttpServletRequest request,
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "20") int limit) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(statisticsService.popularDishes(groupId, limit, user.getId()));
    }
}
