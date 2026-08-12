package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.List;
import java.util.Map;

/**
 * 垂直堆叠容器 — 按序渲染 children，累计 y 偏移。
 * 支持: gap (子元素间距), height (固定高度), separator (底部装饰线)
 */
public class CompositeComponent implements Component {

    private final ComponentRegistry registry;

    public CompositeComponent(ComponentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String type() { return "composite"; }

    @Override
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        List<ComponentDef> children = def.getChildren();
        int totalHeight = 0;
        int totalWidth = ctx.getAvailableWidth();

        if (children != null) {
            for (ComponentDef childDef : children) {
                if (!TextComponent.isVisible(childDef, ctx.getGlobals())) continue;

                Component comp = registry.get(childDef.getType());
                ComponentContext childCtx = ctx.forChild(childDef, totalHeight, totalWidth, ctx.getData());
                int consumed = comp.render(r, childCtx);
                totalHeight += consumed;
                if (def.getGap() > 0) totalHeight += def.getGap();
            }
        }

        // Draw separator line at bottom if configured
        Map<String, Object> sep = def.getSeparator();
        if (sep != null) {
            String color = (String) sep.getOrDefault("color", "#DCDCDC");
            float width = sep.containsKey("thickness")
                    ? ((Number) sep.get("thickness")).floatValue() : 1f;
            int sepY = ctx.getY() + totalHeight;
            r.drawLine(ctx.getX(), sepY, ctx.getX() + totalWidth, sepY, color, width);
            totalHeight += (int) width + 6;
        }

        // Ensure minimum height
        if (def.getHeight() > 0 && totalHeight < def.getHeight()) {
            totalHeight = def.getHeight();
        }

        return totalHeight;
    }
}
