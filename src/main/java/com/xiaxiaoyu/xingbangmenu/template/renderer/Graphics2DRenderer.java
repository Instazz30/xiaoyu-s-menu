package com.xiaxiaoyu.xingbangmenu.template.renderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Graphics2DRenderer implements Renderer {

    private static final Logger log = LoggerFactory.getLogger(Graphics2DRenderer.class);

    private final List<BufferedImage> pages = new ArrayList<>();
    private final FontManager fontManager = new FontManager();

    private BufferedImage currentPage;
    private Graphics2D g;
    private int pageWidth;
    private int pageHeight;
    private String pageBackground = "#FFFFFF";
    private int maxContentY;
    private boolean transparentBackground;

    @Override
    public void begin(int pageWidth, int pageHeight) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        createPage();
    }

    @Override
    public void newPage() {
        pages.add(currentPage);
        disposeGraphics();
        createPage();
    }

    @Override
    public List<BufferedImage> finish() {
        if (currentPage != null) {
            pages.add(currentPage);
            disposeGraphics();
        }
        return new ArrayList<>(pages);
    }

    private void createPage() {
        currentPage = new BufferedImage(pageWidth, pageHeight,
                transparentBackground ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        g = currentPage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (transparentBackground) {
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, pageWidth, pageHeight);
            g.setComposite(AlphaComposite.SrcOver);
        } else {
            fillBackground(pageBackground);
        }
        maxContentY = 0;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    private void disposeGraphics() {
        if (g != null) { g.dispose(); g = null; }
    }

    public void setPageBackground(String colorHex) {
        this.pageBackground = colorHex;
    }

    private void fillBackground(String colorHex) {
        g.setColor(parseColor(colorHex));
        g.fillRect(0, 0, pageWidth, pageHeight);
    }

    // ---- Drawing primitives ----

    @Override
    public int getMaxContentY() { return maxContentY; }

    private void trackY(int y) { if (y > maxContentY) maxContentY = y; }

    @Override
    public void fillRect(int x, int y, int w, int h, String colorHex) {
        g.setColor(parseColor(colorHex));
        g.fillRect(x, y, w, h);
        trackY(y + h);
    }

    @Override
    public void fillRoundRect(int x, int y, int w, int h, int radius, String colorHex) {
        g.setColor(parseColor(colorHex));
        g.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
        trackY(y + h);
    }

    @Override
    public void drawText(String text, int x, int y, int maxWidth, Map<String, Object> style) {
        if (text == null || text.isEmpty()) return;

        String family = string(style, "fontFamily", null);
        int fontSize = integer(style, "fontSize", 20);
        boolean bold = bool(style, "bold", false);
        int fontStyle = bold ? Font.BOLD : Font.PLAIN;
        String colorHex = string(style, "color", "#333333");
        String align = string(style, "align", "left");

        Font font = fontManager.getFont(family, fontStyle, fontSize);
        g.setFont(font);
        g.setColor(parseColor(colorHex));

        FontMetrics fm = g.getFontMetrics();
        String displayText = truncate(text, fm, maxWidth);

        int textX = x;
        if ("center".equals(align)) {
            int tw = fm.stringWidth(displayText);
            textX = x + (maxWidth - tw) / 2;
        } else if ("right".equals(align)) {
            int tw = fm.stringWidth(displayText);
            textX = x + maxWidth - tw;
        }

        g.drawString(displayText, textX, y);
        trackY(y + fm.getDescent());
    }

    @Override
    public int drawWrappedText(String text, int x, int y, int maxWidth, Map<String, Object> style) {
        if (text == null || text.isEmpty()) return 0;

        String family = string(style, "fontFamily", null);
        int fontSize = integer(style, "fontSize", 20);
        boolean bold = bool(style, "bold", false);
        int fontStyle = bold ? Font.BOLD : Font.PLAIN;
        String colorHex = string(style, "color", "#333333");
        String align = string(style, "align", "left");
        int lineGap = integer(style, "lineGap", 4);

        Font font = fontManager.getFont(family, fontStyle, fontSize);
        g.setFont(font);
        g.setColor(parseColor(colorHex));

        FontMetrics fm = g.getFontMetrics();
        List<String> lines = wrapText(text, fm, maxWidth);
        int lineHeight = fm.getHeight();
        int yCursor = y;

        for (String line : lines) {
            int textX = x;
            int tw = fm.stringWidth(line);
            if ("center".equals(align)) {
                textX = x + (maxWidth - tw) / 2;
            } else if ("right".equals(align)) {
                textX = x + maxWidth - tw;
            }
            g.drawString(line, textX, yCursor);
            yCursor += lineHeight + lineGap;
        }
        trackY(yCursor);
        return lines.size() * (lineHeight + lineGap) - lineGap;
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, String color, float width) {
        g.setColor(parseColor(color));
        g.setStroke(new BasicStroke(width));
        g.drawLine(x1, y1, x2, y2);
        trackY(Math.max(y1, y2));
    }

    @Override
    public void drawBackgroundImage(String path, int x, int y, int w, int h) {
        BufferedImage img = loadImage(path);
        if (img == null) return;
        int sw = img.getWidth();
        int sh = img.getHeight();
        if (sw <= 0 || sh <= 0) return;
        // 背景图按宽度铺满、高度等比延伸，只负责铺底，不参与内容高度计算
        double scale = (double) w / sw;
        int nw = w;
        int nh = (int) Math.ceil(sh * scale);
        g.drawImage(img, x, y, nw, nh, null);
    }

    @Override
    public void drawImage(String path, int x, int y, int w, int h,
                          String fit, int borderRadius,
                          String placeholderColor, String placeholderText) {
        BufferedImage img = loadImage(path);
        if (img == null) {
            drawPlaceholder(x, y, w, h, borderRadius, placeholderColor, placeholderText);
            return;
        }

        BufferedImage scaled = scaleToFit(img, w, h, fit);
        int ix = x + (w - scaled.getWidth()) / 2;
        int iy = y + (h - scaled.getHeight()) / 2;

        Shape oldClip = g.getClip();
        if (borderRadius > 0) {
            g.setClip(new RoundRectangle2D.Float(x, y, w, h, borderRadius, borderRadius));
        }
        g.drawImage(scaled, ix, iy, null);
        g.setClip(oldClip);
        trackY(y + h);
    }

    @Override
    public int measureTextWidth(String text, Map<String, Object> style) {
        if (text == null || text.isEmpty()) return 0;
        String family = string(style, "fontFamily", null);
        int fontSize = integer(style, "fontSize", 20);
        boolean bold = bool(style, "bold", false);
        Font font = fontManager.getFont(family, bold ? Font.BOLD : Font.PLAIN, fontSize);
        g.setFont(font);
        return g.getFontMetrics().stringWidth(text);
    }

    @Override
    public int measureTextHeight(Map<String, Object> style) {
        int fontSize = integer(style, "fontSize", 20);
        boolean bold = bool(style, "bold", false);
        Font font = fontManager.getFont(null, bold ? Font.BOLD : Font.PLAIN, fontSize);
        g.setFont(font);
        return g.getFontMetrics().getHeight();
    }

    // ---- Internal helpers ----

    private void drawPlaceholder(int x, int y, int w, int h, int radius,
                                 String bgColor, String text) {
        if (bgColor == null || bgColor.isEmpty()) bgColor = "#F0F0F0";
        if (text == null || text.isEmpty()) text = "暂无图片";
        fillRoundRect(x, y, w, h, radius, bgColor);

        Map<String, Object> style = Map.of("fontSize", 18, "color", "#B4B4B4", "align", "center");
        int textW = w - 8;
        int textY = y + h / 2 + measureTextHeight(style) / 3;
        drawText(text, x + 4, textY, textW, style);
    }

    private BufferedImage loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            if (path.startsWith("https://") || path.startsWith("http://")) {
                return ImageIO.read(URI.create(path).toURL());
            }
            return ImageIO.read(new File(path));
        } catch (Exception e) {
            log.debug("图片加载失败: {}", path);
            return null;
        }
    }

    private BufferedImage scaleToFit(BufferedImage src, int maxW, int maxH, String fit) {
        int sw = src.getWidth();
        int sh = src.getHeight();
        if (sw <= 0 || sh <= 0) return src;

        double scale;
        if ("cover".equals(fit)) {
            scale = Math.max((double) maxW / sw, (double) maxH / sh);
        } else {
            scale = Math.min((double) maxW / sw, (double) maxH / sh);
        }

        int nw = (int) (sw * scale);
        int nh = (int) (sh * scale);

        BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D sg = scaled.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        sg.drawImage(src, 0, 0, nw, nh, null);
        sg.dispose();
        return scaled;
    }

    private String truncate(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        for (int i = text.length() - 1; i > 0; i--) {
            String t = text.substring(0, i) + "...";
            if (fm.stringWidth(t) <= maxWidth) return t;
        }
        return "...";
    }

    /** 按可用宽度逐字符换行（适配中文），保留显式换行符 */
    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lines.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            if (fm.stringWidth(sb.toString() + c) > maxWidth) {
                lines.add(sb.toString());
                sb.setLength(0);
            }
            sb.append(c);
        }
        lines.add(sb.toString());
        return lines;
    }

    // ---- Style helpers ----

    static Color parseColor(String hex) {
        if (hex == null) return Color.BLACK;
        if (hex.startsWith("#")) {
            if (hex.length() == 7) {
                return new Color(Integer.parseInt(hex.substring(1, 3), 16),
                                 Integer.parseInt(hex.substring(3, 5), 16),
                                 Integer.parseInt(hex.substring(5, 7), 16));
            }
            if (hex.length() == 9) { // #RRGGBBAA
                return new Color(Integer.parseInt(hex.substring(1, 3), 16),
                                 Integer.parseInt(hex.substring(3, 5), 16),
                                 Integer.parseInt(hex.substring(5, 7), 16),
                                 Integer.parseInt(hex.substring(7, 9), 16));
            }
            if (hex.length() == 4) {
                int r = Integer.parseInt(hex.substring(1, 2), 16) * 17;
                int g = Integer.parseInt(hex.substring(2, 3), 16) * 17;
                int b = Integer.parseInt(hex.substring(3, 4), 16) * 17;
                return new Color(r, g, b);
            }
        }
        if (hex.startsWith("rgba(") && hex.endsWith(")")) {
            String[] parts = hex.substring(5, hex.length() - 1).split(",");
            if (parts.length == 4) {
                return new Color(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()),
                    (int) (Float.parseFloat(parts[3].trim()) * 255)
                );
            }
        }
        return Color.BLACK;
    }

    static String string(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v instanceof String s ? s : def;
    }

    static int integer(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        return v instanceof Number n ? n.intValue() : def;
    }

    static boolean bool(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        return v instanceof Boolean b ? b : def;
    }
}
