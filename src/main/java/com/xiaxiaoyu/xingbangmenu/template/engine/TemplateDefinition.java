package com.xiaxiaoyu.xingbangmenu.template.engine;

import java.util.List;
import java.util.Map;

/**
 * JSON 模板定义 — 对应 resources/templates/*.json
 */
public class TemplateDefinition {

    private String id;
    private String name;
    private CanvasDef canvas;
    private String background;
    private String backgroundImage;   // page-level background image filename
    private int margin = 40;
    private HeaderDef header;
    private ComponentDef body;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CanvasDef getCanvas() { return canvas; }
    public void setCanvas(CanvasDef canvas) { this.canvas = canvas; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }
    public int getMargin() { return margin; }
    public void setMargin(int margin) { this.margin = margin; }
    public HeaderDef getHeader() { return header; }
    public void setHeader(HeaderDef header) { this.header = header; }
    public ComponentDef getBody() { return body; }
    public void setBody(ComponentDef body) { this.body = body; }

    public static class CanvasDef {
        private int width = 750;
        private int height = 1334;
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
    }

    public static class HeaderDef {
        private int height = 190;
        private String background;
        private String backgroundImage;  // header background image filename
        private List<ComponentDef> children;
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public String getBackground() { return background; }
        public void setBackground(String background) { this.background = background; }
        public String getBackgroundImage() { return backgroundImage; }
        public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }
        public List<ComponentDef> getChildren() { return children; }
        public void setChildren(List<ComponentDef> children) { this.children = children; }
    }

    /**
     * 组件定义 — JSON 中每个组件的反序列化目标。
     * 使用 Map 存储扩展属性，避免每种组件类型定义独立类。
     */
    public static class ComponentDef {
        private String type;
        private String source;          // 数据源绑定: "sections", "items"
        private String text;            // 文本模板: "{{name}}"
        private String defaultText;     // 数据为空时的兜底
        private String visibleIf;       // 可见条件: "showPrice"
        private String visibleIfNot;    // 反向条件
        private Map<String, Object> position;
        private Map<String, Object> style;
        private Map<String, Object> separator;
        private Map<String, Object> placeholder;
        private String path;            // image 组件的路径绑定
        private String fit = "contain"; // image 缩放模式
        private int height;             // 固定高度(0=自动)
        private int borderRadius;
        private int gap;                // 子元素间距
        private int columns;            // grid 列数
        private int cellWidth;          // grid 单元格宽
        private int cellHeight;         // grid 单元格高
        private int reserve = -1;       // grid 单元格内菜名区预留高度(-1=用全局autoReserve)
        private ComponentDef item;      // forEach/grid 的迭代子组件
        private List<ComponentDef> children; // composite 的子组件

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getDefaultText() { return defaultText; }
        public void setDefaultText(String defaultText) { this.defaultText = defaultText; }
        public String getVisibleIf() { return visibleIf; }
        public void setVisibleIf(String visibleIf) { this.visibleIf = visibleIf; }
        public String getVisibleIfNot() { return visibleIfNot; }
        public void setVisibleIfNot(String visibleIfNot) { this.visibleIfNot = visibleIfNot; }
        public Map<String, Object> getPosition() { return position; }
        public void setPosition(Map<String, Object> position) { this.position = position; }
        public Map<String, Object> getStyle() { return style; }
        public void setStyle(Map<String, Object> style) { this.style = style; }
        public Map<String, Object> getSeparator() { return separator; }
        public void setSeparator(Map<String, Object> separator) { this.separator = separator; }
        public Map<String, Object> getPlaceholder() { return placeholder; }
        public void setPlaceholder(Map<String, Object> placeholder) { this.placeholder = placeholder; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getFit() { return fit; }
        public void setFit(String fit) { this.fit = fit; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public int getBorderRadius() { return borderRadius; }
        public void setBorderRadius(int borderRadius) { this.borderRadius = borderRadius; }
        public int getGap() { return gap; }
        public void setGap(int gap) { this.gap = gap; }
        public int getColumns() { return columns; }
        public void setColumns(int columns) { this.columns = columns; }
        public int getCellWidth() { return cellWidth; }
        public void setCellWidth(int cellWidth) { this.cellWidth = cellWidth; }
        public int getCellHeight() { return cellHeight; }
        public void setCellHeight(int cellHeight) { this.cellHeight = cellHeight; }
        public int getReserve() { return reserve; }
        public void setReserve(int reserve) { this.reserve = reserve; }
        public ComponentDef getItem() { return item; }
        public void setItem(ComponentDef item) { this.item = item; }
        public List<ComponentDef> getChildren() { return children; }
        public void setChildren(List<ComponentDef> children) { this.children = children; }
    }
}
