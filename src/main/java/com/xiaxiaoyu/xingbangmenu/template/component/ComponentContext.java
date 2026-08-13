package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;

import java.util.Map;

public class ComponentContext {

    private final int x;
    private final int y;
    private final int availableWidth;
    private final Object data;
    private final Map<String, Object> globals;
    private final ComponentDef def;
    private final int pageHeight;
    private final int margin;

    public ComponentContext(int x, int y, int availableWidth, Object data,
                            Map<String, Object> globals, ComponentDef def,
                            int pageHeight, int margin) {
        this.x = x;
        this.y = y;
        this.availableWidth = availableWidth;
        this.data = data;
        this.globals = globals;
        this.def = def;
        this.pageHeight = pageHeight;
        this.margin = margin;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getAvailableWidth() { return availableWidth; }
    public Object getData() { return data; }
    public Map<String, Object> getGlobals() { return globals; }
    public ComponentDef getDef() { return def; }
    public int getPageHeight() { return pageHeight; }
    public int getMargin() { return margin; }

    public ComponentContext child(int dx, int dy, int childWidth, Object childData) {
        return new ComponentContext(
                x + dx, y + dy, childWidth, childData,
                globals, def, pageHeight, margin);
    }

    /**
     * 创建一个子 context，其 def 来自另一个 ComponentDef (用于 forEach 迭代)。
     */
    public ComponentContext forChild(ComponentDef childDef, int dy, int childWidth, Object childData) {
        return new ComponentContext(
                x, y + dy, childWidth, childData,
                globals, childDef, pageHeight, margin);
    }

    public ComponentContext withDef(ComponentDef newDef) {
        return new ComponentContext(x, y, availableWidth, data, globals, newDef, pageHeight, margin);
    }
}
