package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.List;
import java.util.Map;

/**
 * 遍历数据源，为每个元素渲染 item 子组件。
 * 支持分页：渲染前估算高度，剩余空间不够则 newPage()。
 */
public class ForEachComponent implements Component {

    private final ComponentRegistry registry;

    public ForEachComponent(ComponentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String type() { return "forEach"; }

    @Override
    @SuppressWarnings("unchecked")
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        String source = def.getSource();
        Object sourceData = resolveSource(ctx.getData(), source);
        if (!(sourceData instanceof List<?> items)) return 0;

        ComponentDef itemDef = def.getItem();
        if (itemDef == null) return 0;

        int consumed = 0;
        int pageHeight = ctx.getPageHeight();
        int margin = ctx.getMargin();
        boolean firstOnPage = (ctx.getY() <= margin + 10);

        for (Object item : items) {
            // Estimate height of this iteration's children
            int estimated = estimateHeight(itemDef, item, ctx, ctx.getAvailableWidth());
            int currentY = ctx.getY() + consumed;

            if (!firstOnPage && currentY + estimated > pageHeight - margin) {
                r.newPage();
                consumed = 0;
                firstOnPage = true;
                // Fall through to render current item on the new page
            }
            firstOnPage = false;

            Component comp = registry.get(itemDef.getType());
            ComponentContext itemCtx = ctx.forChild(itemDef, consumed, ctx.getAvailableWidth(), item);
            int h = comp.render(r, itemCtx);
            consumed += h;
            if (def.getGap() > 0) consumed += def.getGap();
        }

        return consumed;
    }

    @SuppressWarnings("unchecked")
    private Object resolveSource(Object data, String source) {
        if (data instanceof Map<?, ?> map) {
            return map.get(source);
        }
        return null;
    }

    /**
     * Recursively estimate the height of a component tree without rendering.
     * Used for page-break decisions.
     */
    @SuppressWarnings("unchecked")
    private int estimateHeight(ComponentDef def, Object data, ComponentContext ctx, int availableWidth) {
        String type = def.getType();
        if (type == null) return 50;

        return switch (type) {
            case "composite" -> {
                List<ComponentDef> children = def.getChildren();
                if (children == null) { yield def.getHeight() > 0 ? def.getHeight() : 30; }
                int h = 0;
                for (ComponentDef child : children) {
                    h += estimateHeight(child, data, ctx, availableWidth);
                    if (def.getGap() > 0) h += def.getGap();
                }
                // Add separator height
                if (def.getSeparator() != null) h += 8;
                yield def.getHeight() > 0 ? Math.max(h, def.getHeight()) : h;
            }
            case "grid" -> {
                Object sourceData = resolveSource(data, def.getSource());
                if (!(sourceData instanceof List<?> items) || items.isEmpty()) yield 0;
                int columns = def.getColumns();
                if (columns <= 0) {
                    Object auto = ctx.getGlobals().get("autoColumns");
                    columns = auto instanceof Number n ? n.intValue() : 2;
                }
                int gap = def.getGap();
                int cellW = (availableWidth - (columns - 1) * gap) / columns;
                int cellH = def.getCellHeight();
                if (cellH <= 0) {
                    Object reserveObj = ctx.getGlobals().get("autoReserve");
                    int reserve = def.getReserve() >= 0
                            ? def.getReserve()
                            : (reserveObj instanceof Number n ? n.intValue() : 0);
                    cellH = cellW + reserve;
                }
                int rows = (int) Math.ceil((double) items.size() / columns);
                yield rows * cellH + (rows - 1) * gap;
            }
            case "image" -> def.getHeight() > 0 ? def.getHeight() : 220;
            case "text", "priceTag" -> 32;
            case "line" -> 6;
            case "rect" -> def.getHeight() > 0 ? def.getHeight() : 10;
            case "forEach" -> {
                Object nestedSource = resolveSource(data, def.getSource());
                if (!(nestedSource instanceof List<?> nestedItems)) yield 0;
                int sum = 0;
                for (Object nestedItem : nestedItems) {
                    sum += estimateHeight(def.getItem(), nestedItem, ctx, availableWidth);
                    if (def.getGap() > 0) sum += def.getGap();
                }
                yield sum;
            }
            default -> 50;
        };
    }
}
