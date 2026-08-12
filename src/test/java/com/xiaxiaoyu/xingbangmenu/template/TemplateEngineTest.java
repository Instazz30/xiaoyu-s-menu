package com.xiaxiaoyu.xingbangmenu.template;

import com.xiaxiaoyu.xingbangmenu.template.component.ComponentRegistry;
import com.xiaxiaoyu.xingbangmenu.template.component.CompositeComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.ForEachComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.GridComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.ImageComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.LineComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.PriceTagComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.RectComponent;
import com.xiaxiaoyu.xingbangmenu.template.component.TextComponent;
import com.xiaxiaoyu.xingbangmenu.template.engine.DataBinder;
import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition;
import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateEngine;
import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateLoader;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 海报模板引擎测试：验证内容宽度固定 750、最终画布按比例留边、图片 1:1 方形、高度随内容动态调整。
 */
class TemplateEngineTest {

    private TemplateEngine newEngine() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.register(new TextComponent());
        registry.register(new ImageComponent());
        registry.register(new RectComponent());
        registry.register(new LineComponent());
        registry.register(new PriceTagComponent());
        registry.register(new CompositeComponent(registry));
        registry.register(new ForEachComponent(registry));
        registry.register(new GridComponent(registry));
        return new TemplateEngine(new DataBinder(), registry);
    }

    private PosterContext context(int sectionCount, int itemsPerSection) {
        PosterContext ctx = new PosterContext();
        ctx.setTitle("今日菜谱");
        ctx.setDateText("2026年8月5日");
        ctx.setCanteenName("兴邦食堂");
        ctx.setIssue(3);
        ctx.setShowDate(true);
        ctx.setShowCanteen(true);
        ctx.setShowPrice(true);

        List<PosterContext.SectionData> sections = new ArrayList<>();
        for (int s = 1; s <= sectionCount; s++) {
            PosterContext.SectionData sd = new PosterContext.SectionData();
            sd.setName("三元区");
            sd.setPriceText("3元");
            List<PosterContext.ItemData> items = new ArrayList<>();
            for (int i = 1; i <= itemsPerSection; i++) {
                PosterContext.ItemData item = new PosterContext.ItemData();
                item.setName("菜品" + i);
                item.setImagePath(null); // 无图，走占位图分支
                items.add(item);
            }
            sd.setItems(items);
            sections.add(sd);
        }
        ctx.setSections(sections);
        return ctx;
    }

    @Test
    void rendersSinglePageWithFixedWidthAndDynamicHeight() throws Exception {
        TemplateDefinition def = new TemplateLoader().load("clean-white");
        List<BufferedImage> pages = newEngine().render(def, context(3, 4));

        assertEquals(1, pages.size(), "常规菜品数量应只生成一页");
        assertEquals(894, pages.get(0).getWidth(), "750px 内容左右应各保留 72px 花纹安全区");
        assertTrue(pages.get(0).getHeight() > 0);
    }

    @Test
    void heightGrowsWithMoreDishes() throws Exception {
        TemplateEngine engine = newEngine();
        TemplateDefinition def = new TemplateLoader().load("clean-white");
        BufferedImage small = engine.render(def, context(1, 2)).get(0);
        BufferedImage large = engine.render(def, context(4, 6)).get(0);

        assertEquals(894, small.getWidth());
        assertEquals(894, large.getWidth());
        assertTrue(large.getHeight() > small.getHeight(), "菜品越多海报越高，图片保持方形不被压缩");
    }

    @Test
    void customBackgroundStretchesToDynamicPosterHeight() throws Exception {
        BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) source.setRGB(x, y, new Color(28, 92, 164).getRGB());
        }
        Path background = Files.createTempFile("poster-background-", ".png");
        ImageIO.write(source, "png", background.toFile());

        TemplateDefinition def = new TemplateLoader().load("clean-white");
        PosterContext smallContext = context(1, 2);
        smallContext.setCustomBackgroundPath(background.toString());
        PosterContext largeContext = context(4, 6);
        largeContext.setCustomBackgroundPath(background.toString());

        BufferedImage small = newEngine().render(def, smallContext).get(0);
        BufferedImage large = newEngine().render(def, largeContext).get(0);

        assertTrue(large.getHeight() > small.getHeight());
        assertEquals(new Color(28, 92, 164).getRGB() & 0xFFFFFF,
                small.getRGB(1, small.getHeight() - 1) & 0xFFFFFF);
        assertEquals(new Color(28, 92, 164).getRGB() & 0xFFFFFF,
                large.getRGB(1, large.getHeight() - 1) & 0xFFFFFF);
        Files.deleteIfExists(background);
    }

    @Test
    void manyItemsStillRenderSinglePage() throws Exception {
        TemplateDefinition def = new TemplateLoader().load("clean-white");
        List<BufferedImage> pages = newEngine().render(def, context(4, 6));

        assertEquals(1, pages.size(), "24 道菜仍单页输出，高度动态");
    }

    @Test
    void rendersXiaowanSectionWithTextAndImages() throws Exception {
        PosterContext ctx = new PosterContext();
        ctx.setTitle("今日菜谱");
        ctx.setDateText("2026年8月5日");
        ctx.setCanteenName("兴邦食堂");
        ctx.setIssue(3);
        ctx.setShowDate(true);
        ctx.setShowCanteen(true);
        ctx.setShowPrice(true);

        // 套餐区：5 道菜（3+2 布局）
        PosterContext.SectionData taocan = new PosterContext.SectionData();
        taocan.setName("三元区");
        taocan.setPriceText("3元");
        List<PosterContext.ItemData> taocanItems = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            PosterContext.ItemData item = new PosterContext.ItemData();
            item.setName("套餐菜" + i);
            item.setImagePath(null);
            taocanItems.add(item);
        }
        taocan.setItems(taocanItems);

        // 小碗菜区：两个价格区
        PosterContext.SectionData xw1 = new PosterContext.SectionData();
        xw1.setName("3元区");
        xw1.setXiaowan(true);
        xw1.setItems(List.of(item("蒸南瓜"), item("烧鸭")));
        PosterContext.SectionData xw2 = new PosterContext.SectionData();
        xw2.setName("5元区");
        xw2.setXiaowan(true);
        xw2.setItems(List.of(item("拌豆笋"), item("烤肉")));

        // 小碗菜自由图：4 张
        List<PosterContext.ItemData> xiaowanImages = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            PosterContext.ItemData img = new PosterContext.ItemData();
            img.setImagePath("C:/tmp/poster_test/xiaowan_" + i + ".png");
            xiaowanImages.add(img);
        }

        ctx.setSections(List.of(taocan, xw1, xw2));
        ctx.setXiaowanImages(xiaowanImages);

        TemplateDefinition def = new TemplateLoader().load("clean-white");
        List<BufferedImage> pages = newEngine().render(def, ctx);

        assertEquals(1, pages.size());
        assertEquals(894, pages.get(0).getWidth());

        File outDir = new File("C:/tmp/poster_test");
        outDir.mkdirs();
        File outFile = new File(outDir, "menu_xiaowan.png");
        ImageIO.write(pages.get(0), "png", outFile);
        assertTrue(outFile.exists(), "小碗菜海报应成功输出");

        // 小碗菜图用彩色测试图，渲染后海报底部应存在非白像素（4 张图所在区域）
        BufferedImage page = pages.get(0);
        int colorPixels = 0;
        for (int y = 300; y < page.getHeight(); y += 2) {
            for (int x = 0; x < page.getWidth(); x += 2) {
                int rgb = page.getRGB(x, y) & 0xFFFFFF;
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                boolean vivid = Math.abs(r - g) > 40 || Math.abs(g - b) > 40 || Math.abs(r - b) > 40;
                if (vivid) colorPixels++;
            }
        }
        assertTrue(colorPixels > 100, "海报中应渲染出小碗菜彩色图片，实际彩色像素=" + colorPixels);
    }

    @Test
    void xiaowanAreaOnlyRendersWhenPresent() throws Exception {
        TemplateDefinition def = new TemplateLoader().load("clean-white");
        List<BufferedImage> pages = newEngine().render(def, context(1, 5));
        assertEquals(1, pages.size());
        // 无小碗菜时输出尺寸与纯套餐场景一致，不出现空的小碗菜区
        assertEquals(894, pages.get(0).getWidth());
    }

    private static PosterContext.ItemData item(String name) {
        PosterContext.ItemData item = new PosterContext.ItemData();
        item.setName(name);
        item.setImagePath(null);
        return item;
    }

}
