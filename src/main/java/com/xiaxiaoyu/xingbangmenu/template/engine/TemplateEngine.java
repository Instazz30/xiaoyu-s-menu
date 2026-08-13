package com.xiaxiaoyu.xingbangmenu.template.engine;

import com.xiaxiaoyu.xingbangmenu.template.PosterContext;
import com.xiaxiaoyu.xingbangmenu.template.component.Component;
import com.xiaxiaoyu.xingbangmenu.template.component.ComponentContext;
import com.xiaxiaoyu.xingbangmenu.template.component.ComponentRegistry;
import com.xiaxiaoyu.xingbangmenu.template.component.GridComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.TextComponent;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Graphics2DRenderer;
import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);
    private static final String BG_DIR = "./uploads/backgrounds/";

    /** 渲染工作画布高度（足够容纳任意菜品数量，输出时再按实际内容裁剪） */
    private static final int LAYOUT_PAGE_HEIGHT = 12000;
    /** 菜单内容与背景蓝色花纹之间的四边安全距离。 */
    private static final int CANVAS_SAFE_INSET = 72;

    private final DataBinder dataBinder;
    private final ComponentRegistry componentRegistry;

    public TemplateEngine(DataBinder dataBinder, ComponentRegistry componentRegistry) {
        this.dataBinder = dataBinder;
        this.componentRegistry = componentRegistry;
    }

    private static String resolveBgPath(String filename) {
        if (filename == null || filename.isEmpty()) return null;
        return java.nio.file.Paths.get(BG_DIR, filename).toAbsolutePath().toString();
    }

    public List<BufferedImage> render(TemplateDefinition def, PosterContext ctx) {
        Map<String, Object> model = dataBinder.toModel(ctx);

        // 图片 1:1 方形布局：按内容量计算列数与单元格尺寸
        applyAutoLayout(def, model);

        Graphics2DRenderer renderer = new Graphics2DRenderer();
        int pageW = def.getCanvas().getWidth();
        int pageH = LAYOUT_PAGE_HEIGHT;
        int margin = def.getMargin();
        boolean hasCustomBackground = ctx.getCustomBackgroundPath() != null;

        String bg = def.getBackground();
        if (!hasCustomBackground && bg != null && !bg.isEmpty()) {
            renderer.setPageBackground(bg);
        }
        renderer.setTransparentBackground(hasCustomBackground);
        renderer.begin(pageW, pageH);

        // Page-level background image (behind all content)
        String pageBgImage = hasCustomBackground ? null : resolveBgPath(def.getBackgroundImage());
        if (pageBgImage != null) {
            renderer.drawBackgroundImage(pageBgImage, 0, 0, pageW, pageH);
        }

        // Phase 1: Header
        TemplateDefinition.HeaderDef header = def.getHeader();
        if (header != null) {
            renderHeader(renderer, def, header, model, pageW, pageH, margin);
        }

        // Phase 2: Body
        TemplateDefinition.ComponentDef body = def.getBody();
        if (body != null) {
            int bodyY = (header != null ? header.getHeight() + 10 : margin);
            renderBody(renderer, def, body, model, margin, bodyY, pageW, pageH);
        }

        // 宽度固定，高度按实际内容动态裁剪
        List<BufferedImage> pages = renderer.finish();
        List<BufferedImage> trimmed = new ArrayList<>();
        for (BufferedImage page : pages) {
            int contentH = Math.min(renderer.getMaxContentY() + margin, page.getHeight());
            int outputW = page.getWidth() + CANVAS_SAFE_INSET * 2;
            int outputH = contentH + CANVAS_SAFE_INSET * 2;
            int offsetX = CANVAS_SAFE_INSET;
            int offsetY = CANVAS_SAFE_INSET;
            BufferedImage cropped = new BufferedImage(outputW, outputH, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = cropped.createGraphics();
            if (hasCustomBackground) {
                BufferedImage background = loadImage(ctx.getCustomBackgroundPath());
                if (background != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(background, 0, 0, outputW, outputH, null);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, outputW, outputH);
                }
            } else {
                g.setColor(bg != null && !bg.isEmpty() ? Color.decode(bg) : Color.WHITE);
                g.fillRect(0, 0, outputW, outputH);
            }
            g.drawImage(page.getSubimage(0, 0, page.getWidth(), contentH), offsetX, offsetY, null);
            g.dispose();
            applyBrandOverlays(cropped, ctx, CANVAS_SAFE_INSET);
            trimmed.add(cropped);
        }
        return trimmed;
    }

    private void applyBrandOverlays(BufferedImage page, PosterContext ctx, int safeInset) {
        drawOverlay(page, ctx.getLogoPath(), "top_left", 120, 120, false, false, safeInset);
        // 二维码固定在右上安全区，不再随内容高度落到右下角。
        drawOverlay(page, ctx.getQrCodePath(), "top_right", 112, 112, true, true, safeInset);
    }

    private void drawOverlay(BufferedImage page, String path, String slot,
                             int width, int height, boolean whiteBackground, boolean topRight,
                             int safeInset) {
        BufferedImage source = loadImage(path);
        if (source == null) return;
        int padding = 40;
        boolean right = topRight || (slot != null && slot.endsWith("right"));
        int x = right ? page.getWidth() - safeInset - padding - width : safeInset + padding;
        int y = safeInset + 32;

        Graphics2D g = page.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (whiteBackground) {
            g.setColor(new Color(255, 255, 255, 235));
            g.fill(new RoundRectangle2D.Float(x - 8, y - 8, width + 16, height + 16, 16, 16));
        }
        double scale = Math.min((double) width / source.getWidth(), (double) height / source.getHeight());
        int drawW = Math.max(1, (int) (source.getWidth() * scale));
        int drawH = Math.max(1, (int) (source.getHeight() * scale));
        g.drawImage(source, x + (width - drawW) / 2, y + (height - drawH) / 2, drawW, drawH, null);
        g.dispose();
    }

    private BufferedImage loadImage(String path) {
        if (path == null || path.isBlank()) return null;
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                return javax.imageio.ImageIO.read(URI.create(path).toURL());
            }
            return javax.imageio.ImageIO.read(new File(path));
        } catch (Exception e) {
            log.warn("模板品牌素材加载失败: {}", path);
            return null;
        }
    }

    /**
     * 方形图片布局：列数与单元格宽高由每个 grid 自行计算（模板写死列数），
     * 此处只把“单元格内菜名区预留高度”写入 model，供 grid 组件使用：
     *   autoReserve — 图片下方菜名区域的固定高度
     */
    private void applyAutoLayout(TemplateDefinition def, Map<String, Object> model) {
        Layout layout = findGridLayout(def.getBody(), model, model);
        model.put("autoReserve", layout.cellReserve);
    }

    /** 网格布局参数：单元格内菜名区域预留高度 */
    private static class Layout {
        int cellReserve;
    }

    /**
     * 在组件树中定位第一个弹性 grid（cellHeight=0），返回其单元格内菜名区域预留高度。
     */
    @SuppressWarnings("unchecked")
    private Layout findGridLayout(TemplateDefinition.ComponentDef def, Object data, Map<String, Object> globals) {
        Layout b = new Layout();
        if (def == null) return b;
        String type = def.getType();
        if (type == null) return b;

        switch (type) {
            case "composite" -> {
                List<TemplateDefinition.ComponentDef> children = def.getChildren();
                if (children != null) {
                    for (TemplateDefinition.ComponentDef child : children) {
                        Layout cb = findGridLayout(child, data, globals);
                        if (cb.cellReserve > 0) return cb;
                    }
                }
            }
            case "grid" -> {
                Object sourceData = resolveSource(data, def.getSource());
                if (sourceData instanceof List<?> items && !items.isEmpty()) {
                    if (def.getCellHeight() <= 0) {
                        b.cellReserve = estimateFixed(def.getItem(), items.get(0), globals);
                    }
                }
            }
            case "forEach" -> {
                Object sourceData = resolveSource(data, def.getSource());
                if (sourceData instanceof List<?> items) {
                    for (Object item : items) {
                        Layout ib = findGridLayout(def.getItem(), item, globals);
                        if (ib.cellReserve > 0) return ib;
                    }
                }
            }
        }
        return b;
    }

    /**
     * 估算组件固定高度（自动图片 height=0 时贡献 0，其余与渲染一致）。
     * 用于计算单元格内菜名区域的高度，保证图片不被压缩。
     */
    private int estimateFixed(TemplateDefinition.ComponentDef def, Object data, Map<String, Object> globals) {
        if (def == null) return 0;
        String type = def.getType();
        if (type == null) return 0;

        return switch (type) {
            case "composite" -> {
                int h = 0;
                List<TemplateDefinition.ComponentDef> children = def.getChildren();
                if (children != null) {
                    for (TemplateDefinition.ComponentDef child : children) {
                        h += estimateFixed(child, data, globals);
                        if (def.getGap() > 0) h += def.getGap();
                    }
                }
                if (def.getHeight() > 0) h = Math.max(h, def.getHeight());
                if (def.getSeparator() != null) h += 7;
                yield h;
            }
            case "text", "priceTag" -> TextComponent.isVisible(def, globals)
                    ? Math.max(24, styleInt(def, "fontSize", 24) + 14) : 0;
            case "image" -> TextComponent.isVisible(def, globals) && def.getHeight() > 0 ? def.getHeight() : 0;
            case "line" -> 6;
            case "rect" -> def.getHeight() > 0 ? def.getHeight() : 10;
            default -> 0;
        };
    }

    private static int styleInt(TemplateDefinition.ComponentDef def, String key, int defVal) {
        Map<String, Object> style = def.getStyle();
        if (style != null && style.get(key) instanceof Number n) return n.intValue();
        return defVal;
    }

    private int resolveColumns(TemplateDefinition.ComponentDef def, Map<String, Object> globals) {
        if (def.getColumns() > 0) return def.getColumns();
        Object auto = globals.get("autoColumns");
        return auto instanceof Number n ? n.intValue() : 2;
    }

    private Object resolveSource(Object data, String source) {
        if (data instanceof Map<?, ?> map) {
            return map.get(source);
        }
        return null;
    }

    private void renderHeader(Renderer r, TemplateDefinition def,
                               TemplateDefinition.HeaderDef header,
                               Map<String, Object> model,
                               int pageW, int pageH, int margin) {
        // Header background image (drawn first, below solid color overlay)
        String headerBgImage = resolveBgPath(header.getBackgroundImage());
        if (headerBgImage != null) {
            r.drawBackgroundImage(headerBgImage, 0, 0, pageW, header.getHeight());
        }

        // Fill header background (solid color, semi-transparent overlay if bg image present)
        String hbg = header.getBackground();
        if (hbg != null && !hbg.isEmpty()) {
            r.fillRect(0, 0, pageW, header.getHeight(), hbg);
        }

        List<TemplateDefinition.ComponentDef> children = header.getChildren();
        if (children == null) return;

        for (TemplateDefinition.ComponentDef compDef : children) {
            if (!checkVisible(compDef, model)) continue;

            Component comp = componentRegistry.get(compDef.getType());
            int availableWidth = pageW - 2 * margin;
            ComponentContext ctx = new ComponentContext(margin, 0, availableWidth,
                    model, model, compDef, pageH, margin);
            comp.render(r, ctx);
        }
    }

    private void renderBody(Renderer r, TemplateDefinition def,
                             TemplateDefinition.ComponentDef bodyDef,
                             Map<String, Object> model,
                             int margin, int startY, int pageW, int pageH) {
        Component comp = componentRegistry.get(bodyDef.getType());
        int availableWidth = pageW - 2 * margin;
        ComponentContext ctx = new ComponentContext(margin, startY, availableWidth,
                model, model, bodyDef, pageH, margin);
        comp.render(r, ctx);
    }

    private boolean checkVisible(TemplateDefinition.ComponentDef def, Map<String, Object> model) {
        String visibleIf = def.getVisibleIf();
        if (visibleIf != null) {
            Object flag = model.get(visibleIf);
            if (flag instanceof Boolean b && !b) return false;
            if (flag == null) return false;
        }
        String visibleIfNot = def.getVisibleIfNot();
        if (visibleIfNot != null) {
            Object flag = model.get(visibleIfNot);
            if (flag instanceof Boolean b && b) return false;
        }
        return true;
    }
}
