package com.xiaxiaoyu.xingbangmenu.template.renderer;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface Renderer {

    void begin(int pageWidth, int pageHeight);

    void newPage();

    List<BufferedImage> finish();

    void fillRect(int x, int y, int w, int h, String colorHex);

    void fillRoundRect(int x, int y, int w, int h, int radius, String colorHex);

    void drawText(String text, int x, int y, int maxWidth, Map<String, Object> style);

    /**
     * 绘制支持自动换行的多行文本，返回实际占用的总高度。
     * 样式支持 "wrap": true 与 "lineGap": N（行间距，默认 4）。
     */
    int drawWrappedText(String text, int x, int y, int maxWidth, Map<String, Object> style);

    void drawLine(int x1, int y1, int x2, int y2, String color, float width);

    void drawImage(String path, int x, int y, int w, int h,
                   String fit, int borderRadius,
                   String placeholderColor, String placeholderText);

    /** 绘制全幅背景图，覆盖指定区域（cover 模式） */
    void drawBackgroundImage(String path, int x, int y, int w, int h);

    int measureTextWidth(String text, Map<String, Object> style);

    int measureTextHeight(Map<String, Object> style);

    /** 返回渲染内容的最大 Y 坐标（用于裁剪多余空白） */
    int getMaxContentY();
}
