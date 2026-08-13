package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.Map;

public class ImageComponent implements Component {

    @Override
    public String type() { return "image"; }

    @Override
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        if (!TextComponent.isVisible(def, ctx.getGlobals())) return 0;

        String path = resolvePath(ctx.getData(), def);
        int h = def.getHeight();
        if (h <= 0) {
            // 自动高度：按 1:1 方形，等于单元格宽度
            h = ctx.getAvailableWidth();
        }
        int w = ctx.getAvailableWidth();
        int radius = def.getBorderRadius();
        String fit = def.getFit() != null ? def.getFit() : "contain";

        Map<String, Object> ph = def.getPlaceholder();
        String phColor = "#F0F0F0";
        String phText = "暂无图片";
        if (ph != null) {
            phColor = (String) ph.getOrDefault("color", phColor);
            phText = (String) ph.getOrDefault("text", phText);
        }

        r.drawImage(path, ctx.getX(), ctx.getY(), w, h, fit, radius, phColor, phText);
        return h;
    }

    static String resolvePath(Object data, ComponentDef def) {
        String template = def.getPath();
        if (template != null && template.startsWith("{{") && template.endsWith("}}")) {
            String key = template.substring(2, template.length() - 2).trim();
            Object val = TextComponent.resolveValue(data, key);
            return val != null ? val.toString() : null;
        }
        return template;
    }
}
