package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.Map;

/**
 * 价格标签 — 目前与 TextComponent 渲染一致，预留独立类型便于未来加图标/币种符号等。
 */
public class PriceTagComponent implements Component {

    @Override
    public String type() { return "priceTag"; }

    @Override
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        if (!TextComponent.isVisible(def, ctx.getGlobals())) return 0;

        String text = TextComponent.resolveText(ctx.getData(), def);
        if (text == null || text.isEmpty()) return 0;

        Map<String, Object> style = def.getStyle();
        if (style == null) style = Map.of();
        style = TextComponent.mergeAlign(style, "right");

        int aw = ctx.getAvailableWidth();
        int th = r.measureTextHeight(style);
        r.drawText(text, ctx.getX(), ctx.getY() + th, aw, style);
        return th + 4;
    }
}
