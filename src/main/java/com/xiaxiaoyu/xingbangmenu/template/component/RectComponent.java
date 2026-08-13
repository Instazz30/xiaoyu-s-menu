package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.Map;

public class RectComponent implements Component {

    @Override
    public String type() { return "rect"; }

    @Override
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        int w = ctx.getAvailableWidth();
        int h = def.getHeight() > 0 ? def.getHeight() : w;
        int radius = def.getBorderRadius();
        Map<String, Object> style = def.getStyle();
        String color = "#FFFFFF";
        if (style != null && style.containsKey("background")) {
            color = (String) style.get("background");
        }

        if (radius > 0) {
            r.fillRoundRect(ctx.getX(), ctx.getY(), w, h, radius, color);
        } else {
            r.fillRect(ctx.getX(), ctx.getY(), w, h, color);
        }
        return h;
    }
}
