package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.Map;

public class LineComponent implements Component {

    @Override
    public String type() { return "line"; }

    @Override
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        Map<String, Object> sep = def.getSeparator();
        if (sep == null) return 0;

        String color = (String) sep.getOrDefault("color", "#DCDCDC");
        float width = sep.containsKey("thickness")
                ? ((Number) sep.get("thickness")).floatValue() : 1f;

        int x1 = ctx.getX();
        int x2 = ctx.getX() + ctx.getAvailableWidth();
        int y = ctx.getY();
        r.drawLine(x1, y, x2, y, color, width);
        return (int) width + 4;
    }
}
