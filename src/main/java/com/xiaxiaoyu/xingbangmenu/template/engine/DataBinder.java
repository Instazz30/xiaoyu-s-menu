package com.xiaxiaoyu.xingbangmenu.template.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaxiaoyu.xingbangmenu.template.PosterContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 将 PosterContext 转换为模板引擎可用的 Map 模型。
 * 使用 Jackson 递归转换 POJO，并将小碗菜区域从 sections 中拆出：
 *   sections         — 套餐区域（图片网格渲染）
 *   xiaowanSections  — 小碗菜区域（价格区文本行渲染，含 itemsText）
 *   xiaowanImages    — 小碗菜自由图
 *   hasXiaowan       — 是否存在小碗菜区域
 */
public class DataBinder {

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public Map<String, Object> toModel(PosterContext ctx) {
        Map<String, Object> model = mapper.convertValue(ctx, LinkedHashMap.class);
        model.put("issueText", chineseIssue(ctx.getIssue()));

        // 拆分小碗菜区域
        List<Map<String, Object>> all = castList(model.get("sections"));
        List<Map<String, Object>> normal = new ArrayList<>();
        List<Map<String, Object>> xiaowan = new ArrayList<>();
        if (all != null) {
            for (Map<String, Object> s : all) {
                boolean isXw = Boolean.TRUE.equals(s.get("xiaowan"))
                        || Boolean.TRUE.equals(s.get("isXiaowan"));
                if (isXw) {
                    s.put("itemsText", joinItemNames(s.get("items")));
                    xiaowan.add(s);
                } else {
                    normal.add(s);
                }
            }
        }
        model.put("sections", normal);
        model.put("xiaowanSections", xiaowan);
        model.put("hasXiaowan", !xiaowan.isEmpty());
        return model;
    }

    private String chineseIssue(Integer issue) {
        return switch (issue == null ? 1 : issue) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            default -> String.valueOf(issue);
        };
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object obj) {
        return obj instanceof List<?> list ? (List<Map<String, Object>>) list : null;
    }

    /** 将区域内的菜名用顿号连接，如 "蒸南瓜、烧鸭" */
    private String joinItemNames(Object itemsObj) {
        if (!(itemsObj instanceof List<?> items)) return "";
        return items.stream()
                .map(it -> it instanceof Map<?, ?> m && m.get("name") != null
                        ? m.get("name").toString() : "")
                .filter(n -> !n.isEmpty())
                .collect(Collectors.joining("、"));
    }
}
