package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.Map;

public class TextComponent implements Component {

    @Override
    public String type() { return "text"; }

    @Override
    @SuppressWarnings("unchecked")
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        String text = resolveText(ctx.getData(), def);
        if (text == null || text.isEmpty()) return 0;

        if (!isVisible(def, ctx.getGlobals())) return 0;

        Map<String, Object> pos = def.getPosition();
        Map<String, Object> style = def.getStyle();
        if (style == null) style = Map.of();

        int availableWidth = ctx.getAvailableWidth();
        int textHeight = r.measureTextHeight(style);

        int drawX = ctx.getX();
        int drawY = ctx.getY() + textHeight;
        int maxWidth = availableWidth;

        // Handle position x modes — keep drawX at left edge, use text-align for centering
        if (pos != null) {
            Object xv = pos.get("x");
            if (xv instanceof Number nx) {
                // 绝对 x 偏移（相对容器左边缘）
                drawX = ctx.getX() + nx.intValue();
            } else if ("center".equals(xv)) {
                style = mergeAlign(style, "center");
            } else if ("right".equals(xv)) {
                style = mergeAlign(style, "right");
            }
        }
        // If position has explicit "y" (absolute from parent), override drawY
        if (pos != null && pos.containsKey("y")) {
            Object yv = pos.get("y");
            if ("center".equals(yv)) {
                // center vertically in parent's height
                int parentH = ctx.getDef().getHeight();
                if (parentH > 0) {
                    drawY = ctx.getY() + (parentH + textHeight) / 2;
                }
            } else if (yv instanceof Number) {
                drawY = ctx.getY() + ((Number) yv).intValue() + textHeight;
            }
        }

        if (style.get("wrap") instanceof Boolean b && b) {
            int wrappedHeight = r.drawWrappedText(text, drawX, drawY, maxWidth, style);
            return wrappedHeight + 4;
        }
        r.drawText(text, drawX, drawY, maxWidth, style);
        return textHeight + 4;
    }

    // ---- helpers ----

    @SuppressWarnings("unchecked")
    static String resolveText(Object data, ComponentDef def) {
        String template = def.getText();
        if (template == null) return def.getDefaultText();

        if (template.startsWith("{{") && template.endsWith("}}")
                && template.indexOf("{{", 2) < 0) {
            String key = template.substring(2, template.length() - 2).trim();
            Object val = resolveValue(data, key);
            if (val != null) return val.toString();
            return def.getDefaultText();
        }

        // 内嵌占位符：如 "第{{issue}}期食堂菜单"
        if (template.contains("{{")) {
            StringBuilder sb = new StringBuilder();
            boolean anyBound = false;
            int idx = 0;
            while (idx < template.length()) {
                int start = template.indexOf("{{", idx);
                if (start < 0) {
                    sb.append(template, idx, template.length());
                    break;
                }
                sb.append(template, idx, start);
                int end = template.indexOf("}}", start + 2);
                if (end < 0) {
                    sb.append(template.substring(start));
                    break;
                }
                String key = template.substring(start + 2, end).trim();
                Object val = resolveValue(data, key);
                if (val != null) {
                    sb.append(val);
                    anyBound = true;
                }
                idx = end + 2;
            }
            String bound = sb.toString();
            if (!anyBound && def.getDefaultText() != null) return def.getDefaultText();
            return bound.isEmpty() ? def.getDefaultText() : bound;
        }
        return template;
    }

    static Object resolveValue(Object data, String key) {
        if (data instanceof Map<?, ?> map) {
            return map.get(key);
        }
        return null;
    }

    public static boolean isVisible(ComponentDef def, Map<String, Object> globals) {
        if (def.getVisibleIf() != null) {
            Object flag = globals.get(def.getVisibleIf());
            if (flag instanceof Boolean b && !b) return false;
            if (flag == null) return false;
        }
        if (def.getVisibleIfNot() != null) {
            Object flag = globals.get(def.getVisibleIfNot());
            if (flag instanceof Boolean b && b) return false;
        }
        return true;
    }

    static Map<String, Object> mergeAlign(Map<String, Object> style, String align) {
        if (style.containsKey("align")) return style;
        java.util.Map<String, Object> merged = new java.util.LinkedHashMap<>(style);
        merged.put("align", align);
        return merged;
    }

    static String string(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v instanceof String s ? s : def;
    }
}
