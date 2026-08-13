package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsService {

    private final MenuRecipeMapper recipeMapper;
    private final MenuItemMapper itemMapper;
    private final PermissionService permissionService;

    public StatisticsService(MenuRecipeMapper recipeMapper,
                              MenuItemMapper itemMapper,
                              PermissionService permissionService) {
        this.recipeMapper = recipeMapper;
        this.itemMapper = itemMapper;
        this.permissionService = permissionService;
    }

    public Map<String, Object> overview(Long groupId, Long userId) {
        permissionService.requireMember(groupId, userId);
        List<Map<String, Object>> statusCounts = recipeMapper.countByStatus(groupId);
        long totalRecipes = 0;
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Map<String, Object> row : statusCounts) {
            long cnt = ((Number) row.get("cnt")).longValue();
            String status = (String) row.get("status");
            totalRecipes += cnt;
            byStatus.put(status, cnt);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRecipes", totalRecipes);
        result.put("byStatus", byStatus);
        return result;
    }

    public List<Map<String, Object>> trends(Long groupId, int months, Long userId) {
        permissionService.requireMember(groupId, userId);
        List<Map<String, Object>> raw = recipeMapper.countByMonth(groupId, months);
        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", row.get("month"));
            item.put("count", ((Number) row.get("cnt")).longValue());
            trend.add(item);
        }
        return trend;
    }

    public List<Map<String, Object>> popularDishes(Long groupId, int limit, Long userId) {
        permissionService.requireMember(groupId, userId);
        List<Map<String, Object>> raw = itemMapper.countByDishName(groupId, limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", row.get("name"));
            item.put("count", ((Number) row.get("cnt")).longValue());
            result.add(item);
        }
        return result;
    }
}
