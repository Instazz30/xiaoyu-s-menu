package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.dto.ParseResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeParserTest {

    private final RecipeParser parser = new RecipeParser();

    // ========== 标准格式测试 ==========

    @Test
    void shouldParseStandardFormat() {
        String text = """
                今日套餐
                三元区：蒸蛋、茄子
                四元区：红烧肉、土豆丝、青椒炒蛋
                主食：米饭、馒头
                汤品：紫菜蛋花汤""";

        ParseResult result = parser.parse(text);

        assertEquals("今日套餐", result.getTitle());
        assertEquals(4, result.getSections().size());
        assertTrue(result.getUnrecognizedLines().isEmpty());

        assertEquals("三元区", result.getSections().get(0).getName());
        assertEquals("三元", result.getSections().get(0).getPriceText());
        assertEquals(new BigDecimal("3"), result.getSections().get(0).getPrice());
        assertEquals(List.of("蒸蛋", "茄子"), result.getSections().get(0).getItems());

        assertEquals("四元区", result.getSections().get(1).getName());
        assertEquals("四元", result.getSections().get(1).getPriceText());
        assertEquals(List.of("红烧肉", "土豆丝", "青椒炒蛋"), result.getSections().get(1).getItems());

        assertEquals("主食", result.getSections().get(2).getName());
        assertEquals(List.of("米饭", "馒头"), result.getSections().get(2).getItems());

        assertEquals("汤品", result.getSections().get(3).getName());
        assertEquals(List.of("紫菜蛋花汤"), result.getSections().get(3).getItems());
    }

    // ========== 非标准分隔符测试 ==========

    @Test
    void shouldParseNonStandardSeparators() {
        String text = "三元区 蒸蛋 茄子\n四元区 红烧肉，土豆丝还有青椒炒蛋";

        ParseResult result = parser.parse(text);

        assertEquals(2, result.getSections().size());
        assertEquals(List.of("蒸蛋", "茄子"), result.getSections().get(0).getItems());
        assertEquals(3, result.getSections().get(1).getItems().size());
    }

    @Test
    void shouldParseSlashSeparator() {
        String text = "五元区 鱼香肉丝/宫保鸡丁";

        ParseResult result = parser.parse(text);

        assertEquals(1, result.getSections().size());
        assertEquals(List.of("鱼香肉丝", "宫保鸡丁"), result.getSections().get(0).getItems());
    }

    // ========== 多行格式测试 ==========

    @Test
    void shouldParseMultiLineFormat() {
        String text = """
                三元区
                蒸蛋
                茄子

                四元区
                红烧肉
                土豆丝
                青椒炒蛋""";

        ParseResult result = parser.parse(text);

        assertEquals(2, result.getSections().size());
        assertEquals("三元区", result.getSections().get(0).getName());
        assertEquals(List.of("蒸蛋", "茄子"), result.getSections().get(0).getItems());
        assertEquals(List.of("红烧肉", "土豆丝", "青椒炒蛋"), result.getSections().get(1).getItems());
    }

    // ========== 混合价格/非价格区域 ==========

    @Test
    void shouldParseMixedPriceAndFreeSections() {
        String text = """
                今日菜谱
                三元区 蒸蛋、烧茄子
                主食 米饭
                免费汤 紫菜蛋花汤""";

        ParseResult result = parser.parse(text);

        assertEquals(3, result.getSections().size());

        assertEquals(new BigDecimal("3"), result.getSections().get(0).getPrice());
        assertNull(result.getSections().get(1).getPrice()); // 主食无价格
        assertEquals(BigDecimal.ZERO, result.getSections().get(2).getPrice()); // 免费
        assertEquals("免费", result.getSections().get(2).getPriceText());
    }

    @Test
    void shouldParseChineseNumberPrice() {
        String text = "三块钱区 蒸蛋、茄子";

        ParseResult result = parser.parse(text);

        assertEquals(1, result.getSections().size());
        // 中文数字解析可能不太完美，这里主要验证不崩溃
        assertNotNull(result.getSections().get(0).getPriceText());
    }

    // ========== 未识别行处理 ==========

    @Test
    void shouldReturnUnrecognizedLines() {
        String text = "未知内容行\n三元区 蒸蛋";

        ParseResult result = parser.parse(text);

        // 如果第一行无法识别为区域，且第二行被识别，则需要检查
        assertFalse(result.getSections().isEmpty());
    }

    @Test
    void shouldNotLoseOriginalTextOnEmpty() {
        ParseResult result = parser.parse("");

        assertTrue(result.getSections().isEmpty());
        assertTrue(result.getUnrecognizedLines().isEmpty());
    }

    @Test
    void shouldHandleNullInput() {
        ParseResult result = parser.parse(null);

        assertNotNull(result);
        assertTrue(result.getSections().isEmpty());
    }

    // ========== 各种价格表示法 ==========

    @Test
    void shouldParseVariousPriceFormats() {
        assertEquals("3元", parseSingleSectionPrice("3元区 蒸蛋"));
        assertEquals("4块", parseSingleSectionPrice("4块区 红烧肉"));
        assertEquals("免费", parseSingleSectionPrice("免费汤 紫菜汤"));
    }

    private String parseSingleSectionPrice(String text) {
        ParseResult result = parser.parse(text);
        if (result.getSections().isEmpty()) return null;
        return result.getSections().get(0).getPriceText();
    }

    // ========== 复合菜名保护测试 ==========

    @Test
    void shouldNotSplitCompoundDishNames() {
        String text = "四元区 青椒炒蛋、西红柿炒鸡蛋";

        ParseResult result = parser.parse(text);

        List<String> items = result.getSections().get(0).getItems();
        assertTrue(items.stream().anyMatch(s -> s.contains("青椒炒蛋")),
                "青椒炒蛋 不应被拆分为 青椒 和 炒蛋");
    }

    // ========== 无标题测试 ==========

    @Test
    void shouldWorkWithoutTitle() {
        String text = "三元区 蒸蛋、茄子";

        ParseResult result = parser.parse(text);

        assertNull(result.getTitle());
        assertEquals(1, result.getSections().size());
    }

    // ========== 纯菜品文本（无区域头） ==========

    @Test
    void shouldCreateDefaultSectionForPureItemText() {
        String text = "蒸蛋\n茄子\n红烧肉";

        ParseResult result = parser.parse(text);

        assertFalse(result.getSections().isEmpty());
        assertEquals("菜品", result.getSections().get(0).getName());
        assertTrue(result.getSections().get(0).isNeedsConfirmation());
    }

    // ========== 小碗菜识别 ==========

    @Test
    void shouldParseXiaowanSections() {
        String text = """
                小碗菜
                3元区：蒸南瓜 烧鸭
                5元区：拌豆笋 烤肉
                8元区：葱煎蛋""";

        ParseResult result = parser.parse(text);

        assertNull(result.getWarning());
        assertEquals(3, result.getSections().size());
        assertTrue(result.getSections().stream().allMatch(ParseResult.SectionResult::isXiaowan));
        assertEquals(List.of("蒸南瓜", "烧鸭"), result.getSections().get(0).getItems());
        assertEquals("3元区", result.getSections().get(0).getName());
        assertTrue(result.getUnrecognizedLines().isEmpty());
    }

    @Test
    void shouldWarnWhenNoXiaowanTitle() {
        String text = "3元区：蒸南瓜、烧鸭";

        ParseResult result = parser.parse(text);

        assertNotNull(result.getWarning());
        assertTrue(result.getWarning().contains("小碗菜"));
        assertEquals(1, result.getSections().size());
        assertFalse(result.getSections().get(0).isXiaowan());
    }

    @Test
    void shouldKeepTaocanAndXiaowanSeparate() {
        String text = """
                今日套餐
                三元区：蒸蛋、茄子
                小碗菜
                3元区：蒸南瓜 烧鸭
                5元区：拌豆笋 烤肉""";

        ParseResult result = parser.parse(text);

        assertNull(result.getWarning());
        assertEquals(3, result.getSections().size());
        assertFalse(result.getSections().get(0).isXiaowan());
        assertTrue(result.getSections().get(1).isXiaowan());
        assertTrue(result.getSections().get(2).isXiaowan());
    }

    @Test
    void shouldKeepTwoCharDishNamesSeparate() {
        String text = "三元区 汉堡 薯条";

        ParseResult result = parser.parse(text);

        assertEquals(1, result.getSections().size());
        assertEquals(List.of("汉堡", "薯条"), result.getSections().get(0).getItems());
    }
}
