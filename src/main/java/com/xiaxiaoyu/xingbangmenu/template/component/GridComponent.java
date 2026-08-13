package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

import java.util.List;
import java.util.Map;

/**
 * 网格布局 — columns 列，遍历数据源，为每个元素渲染 item 子组件。
 */
public class GridComponent implements Component {

    private final ComponentRegistry registry;

    public GridComponent(ComponentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String type() { return "grid"; }

    @Override
    @SuppressWarnings("unchecked")
    public int render(Renderer r, ComponentContext ctx) {
        ComponentDef def = ctx.getDef();
        String source = def.getSource();
        Object sourceData = resolveSource(ctx.getData(), source);
        if (!(sourceData instanceof List<?> items) || items.isEmpty()) return 0;

        ComponentDef cellDef = def.getItem();
        if (cellDef == null) return 0;

        int columns = def.getColumns();
        if (columns <= 0) {
            Object auto = ctx.getGlobals().get("autoColumns");
            columns = auto instanceof Number n ? n.intValue() : 2;
        }
        int gap = def.getGap();
        int cellW = def.getCellWidth() > 0
                ? def.getCellWidth()
                : (ctx.getAvailableWidth() - (columns - 1) * gap) / columns;
        int cellH = def.getCellHeight();
        if (cellH <= 0) {
            // 方形图片 + 菜名预留：单元格高度 = 列宽 + 预留（默认取全局 autoReserve）
            Object reserveObj = ctx.getGlobals().get("autoReserve");
            int reserve = def.getReserve() >= 0
                    ? def.getReserve()
                    : (reserveObj instanceof Number n ? n.intValue() : 0);
            cellH = cellW + reserve;
        }

        for (int i = 0; i < items.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int cx = ctx.getX() + col * (cellW + gap);
            int cy = ctx.getY() + row * (cellH + gap);

            Component comp = registry.get(cellDef.getType());
            // Create a child context positioned at the cell
            ComponentContext cellCtx = new ComponentContext(
                    cx, cy, cellW, items.get(i),
                    ctx.getGlobals(), cellDef,
                    ctx.getPageHeight(), ctx.getMargin()
            );
            comp.render(r, cellCtx);
        }

        int rows = (int) Math.ceil((double) items.size() / columns);
        return rows * cellH + (rows - 1) * gap;
    }

    /**
     * 按菜品总数自适应列数：少则 2 列大图，多则 3/4 列保证一屏放下。
     */
    public static int autoColumns(int itemCount) {
        if (itemCount <= 8) return 2;
        if (itemCount <= 18) return 3;
        return 4;
    }

    @SuppressWarnings("unchecked")
    private Object resolveSource(Object data, String source) {
        if (data instanceof Map<?, ?> map) {
            return map.get(source);
        }
        return null;
    }
}
