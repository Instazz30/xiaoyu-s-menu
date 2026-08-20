package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.dto.ParseResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecipeParser {

    // 中文字符 → 阿拉伯数字映射
    private static final java.util.Map<Character, Integer> CHINESE_NUM = java.util.Map.ofEntries(
            java.util.Map.entry('零', 0), java.util.Map.entry('一', 1), java.util.Map.entry('二', 2),
            java.util.Map.entry('三', 3), java.util.Map.entry('四', 4), java.util.Map.entry('五', 5),
            java.util.Map.entry('六', 6), java.util.Map.entry('七', 7), java.util.Map.entry('八', 8),
            java.util.Map.entry('九', 9), java.util.Map.entry('十', 10), java.util.Map.entry('两', 2)
    );

    // 价格模式: 中文数字+元/块/钱/块钱, 或阿拉伯数字+元/块/钱, 或免费
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "([零一二三四五六七八九十两]+|[0-9]+\\.?[0-9]*)\\s*(元|块|块钱|元钱)|(免费)"
    );

    // 区域关键词（跟在价格后面表示区域）
    private static final Pattern SECTION_KEYWORD = Pattern.compile(
            "(区|套餐|主食|汤品?|小菜|凉菜|荤菜|素菜|甜点|饮品|饮料|面食|盖浇|盖饭|炒菜|烧菜|炖菜|碗菜)"
    );

    // 标题关键词
    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "^(今日|今天|本周|今日份|本日).{0,5}(菜谱|菜单|套餐|菜式|菜品)"
    );

    // 小碗菜标题行（独立成行，如 "小碗菜" 或 "小碗菜："）
    private static final Pattern XIAOWAN_TITLE = Pattern.compile(
            "小碗菜(区)?[:：]?"
    );

    // 分隔符
    private static final Pattern ITEM_SEPARATOR = Pattern.compile(
            "[、，,;；/\\s]+|和(?!\\S{2})|还有(?!\\S{2})|包括(?!\\S{2})"
    );

    public ParseResult parse(String text) {
        ParseResult result = new ParseResult();
        result.setSections(new ArrayList<>());
        result.setUnrecognizedLines(new ArrayList<>());

        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        //统一换行格式，并且按换行符分割后存入rawLines数组
        String[] rawLines = text.replace("\r\n", "\n").replace("\r", "\n").split("\n");
        List<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            //trim函数，删除字符串首位的空字符
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }

        if (lines.isEmpty()) return result;

        int idx = 0;

        // 检测标题（第一行匹配）
        if (TITLE_PATTERN.matcher(lines.get(idx)).find()) {
            result.setTitle(lines.get(idx));
            idx++;
        }

        ParseResult.SectionResult currentSection = null;
        List<String> pendingItems = new ArrayList<>();
        boolean xiaowanMode = false;

        while (idx < lines.size()) {
            String line = lines.get(idx);

            // 小碗菜标题行：进入小碗菜模式，其后价格区均标记为小碗菜区
            if (XIAOWAN_TITLE.matcher(line).matches()) {
                xiaowanMode = true;
                idx++;
                continue;
            }

            SectionMatch match = tryMatchSection(line);

            if (match != null) {
                // 将上一区域的菜品存档
                flushSection(result, currentSection, pendingItems);
                pendingItems.clear();
                //创建新区
                currentSection = new ParseResult.SectionResult();
                currentSection.setName(match.name);
                currentSection.setPriceText(match.priceText);
                currentSection.setPrice(match.price);
                currentSection.setNeedsConfirmation(match.needsConfirmation);
                currentSection.setXiaowan(xiaowanMode);

                if (match.remaining != null && !match.remaining.isEmpty()) {
                    pendingItems.addAll(splitItems(match.remaining));
                }
            } else if (currentSection != null) {
                // 属于当前区域的菜品行
                List<String> items = splitItems(line);
                if (!items.isEmpty()) {
                    pendingItems.addAll(items);
                } else {
                    result.getUnrecognizedLines().add(line);
                }
            } else {
                result.getUnrecognizedLines().add(line);
            }

            idx++;
        }

        // 保存最后一个区域
        flushSection(result, currentSection, pendingItems);

        // 后处理：拆分内嵌的区域关键词（如"小碗菜"出现在菜品列表中）
        result.setSections(splitEmbeddedSections(result.getSections()));

        // 后处理：合并无菜品的标签区域到下一个区域（如独立的"小碗菜"标题）
        result.setSections(mergeLabelSections(result.getSections()));

        // 兜底：无区域时，将所有未识别行当作默认区域
        if (result.getSections().isEmpty() && !result.getUnrecognizedLines().isEmpty()) {
            ParseResult.SectionResult defaultSection = new ParseResult.SectionResult();
            defaultSection.setName("菜品");
            defaultSection.setNeedsConfirmation(true);
            List<String> all = new ArrayList<>();
            for (String ul : result.getUnrecognizedLines()) {
                all.addAll(splitItems(ul));
            }
            defaultSection.setItems(all);
            result.getSections().add(defaultSection);
            result.setUnrecognizedLines(new ArrayList<>());
        }

        // 规范提示：未检测到小碗菜标题行时提醒用户补充
        if (!xiaowanMode) {
            result.setWarning("未检测到「小碗菜」标题行。若本次菜单包含小碗菜，请在文本中补充一行「小碗菜」，其后的价格区（如 3元区：蒸南瓜、烧鸭）将被识别为小碗菜。");
        }

        return result;
    }

    // ---- 区域头匹配 ----

    private SectionMatch tryMatchSection(String line) {
        // Step 1: 按冒号分割，分出区域头和同行菜品
        String headerPart = line;
        String itemPart = null;

        int colonIdx = line.indexOf('：');
        if (colonIdx < 0) colonIdx = line.indexOf(':');
        if (colonIdx > 0) {
            headerPart = line.substring(0, colonIdx).trim();
            String rest = line.substring(colonIdx + 1).trim();
            if (!rest.isEmpty()) itemPart = rest;
        } else {
            // 没有冒号，尝试在区域关键词后切分
            // 例如 "三元区 蒸蛋 茄子" → 在 "区" 后找第一个菜品分隔符
            Matcher kw = SECTION_KEYWORD.matcher(line);
            while (kw.find()) {
                int afterKw = kw.end();
                // 关键词后面是分隔符（空格/标点），则后面是菜品
                if (afterKw < line.length()) {
                    char next = line.charAt(afterKw);
                    if (next == ' ' || next == '、' || next == '，' || next == ',' || next == '；' || next == ';' || next == '/') {
                        headerPart = line.substring(0, afterKw).trim();
                        itemPart = line.substring(afterKw).replaceAll("^[\\s、，,;；/]+", "").trim();
                        if (itemPart.isEmpty()) itemPart = null;
                        break;
                    }
                }
            }
        }

        // Step 2: 从 headerPart 中提取区域名和价格
        if (!containsSectionKeyword(headerPart)) {
            // 也检查是否是纯非价格区域头
            for (String nps : NON_PRICE_SECTIONS) {
                if (headerPart.startsWith(nps) || headerPart.equals(nps)) {
                    // 匹配
                }
            }
            return null;
        }

        String sectionName = headerPart;
        String priceText = null;
        BigDecimal price = null;
        boolean needsConfirmation = false;

        // 提取价格
        Matcher priceMatcher = PRICE_PATTERN.matcher(headerPart);
        if (priceMatcher.find()) {
            String numPart = priceMatcher.group(1);
            String unitPart = priceMatcher.group(2);
            String freePart = priceMatcher.group(3);

            if ("免费".equals(freePart)) {
                priceText = "免费";
                price = BigDecimal.ZERO;
            } else if (numPart != null && unitPart != null) {
                priceText = numPart + unitPart;
                BigDecimal bd = parseChineseNumber(numPart);
                if (bd == null) {
                    try {
                        bd = new BigDecimal(numPart);
                    } catch (NumberFormatException e) {
                        needsConfirmation = true;
                    }
                }
                price = bd;
            }
        }

        return new SectionMatch(sectionName, priceText, price, itemPart, needsConfirmation);
    }

    private boolean containsSectionKeyword(String s) {
        return SECTION_KEYWORD.matcher(s).find();
    }

    List<String> splitItems(String line) {
        if (line == null || line.isEmpty()) return List.of();

        // 对空格做特殊处理：单个空格可能是分隔符也有可能是自然语言连接
        // 先统一用顿号替换常见分隔符（保护菜名中不拆分）
        String normalized = line
                .replace("，", "、")
                .replace(",", "、")
                .replace(";", "、")
                .replace("；", "、")
                .replace("/", "、")
                .replace("还有", "、")
                .replace("包括", "、")
                .replaceAll("\\s*和\\s*", "、");

        // 按序列分隔符拆分
        String[] parts = ITEM_SEPARATOR.split(normalized);
        List<String> items = new ArrayList<>();
        for (String part : parts) {
            String t = part.trim();
            if (!t.isEmpty()) {
                items.add(t);
            }
        }

        // 合并误拆的复合菜名
        return mergeCompoundItems(items);
    }

    private List<String> mergeCompoundItems(List<String> items) {
        List<String> merged = new ArrayList<>();
        for (String item : items) {
            //检查数组里最后一个元素是否不完整
            if (!merged.isEmpty() && isIncompleteItem(merged.get(merged.size() - 1))) {
                //这里拿到的是杀一个item
                String prev = merged.remove(merged.size() - 1);
                //拼接下一个item
                merged.add(prev + ("、".equals(item) ? "" : item));
            } else {
                merged.add(item);
            }
        }
        return merged;
    }

    private boolean isIncompleteItem(String name) {
        if (name.length() <= 2) {
            // 两个字的菜名（汉堡、薯条、排骨、鸡腿…）视为完整菜名，
            // 避免与下一个菜名误合并（如 "汉堡 薯条" 被拼成 "汉堡薯条"）
            return false;
        }
        return name.endsWith("炒") || name.endsWith("烧") || name.endsWith("炖")
                || name.endsWith("焖") || name.endsWith("炸") || name.endsWith("熘")
                || name.endsWith("爆") || name.endsWith("烩") || name.endsWith("焗");
    }

    private BigDecimal parseChineseNumber(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ignored) {}

        // 中文数字：三 → 3, 十二 → 12, 二十 → 20
        int total = 0;
        int section = 0;
        boolean hasDeci = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Integer val = CHINESE_NUM.get(c);
            if (val == null) return null;
            if (val >= 10) {
                if (section == 0) section = 1;
                total += section * val;
                section = 0;
                hasDeci = true;
            } else {
                section = val;
            }
        }
        total += section;
        if (!hasDeci && text.length() == 1) {
            // 单个数字如 "三" → 3
        }
        return BigDecimal.valueOf(total);
    }

    //将上一段内容保存
    private void flushSection(ParseResult result, ParseResult.SectionResult section, List<String> items) {
        if (section != null && !items.isEmpty()) {
            section.setItems(new ArrayList<>(items));
            result.getSections().add(section);
        } else if (section != null && items.isEmpty()) {
            // 区域头没有菜品，仍然保留
            section.setItems(new ArrayList<>());
            result.getSections().add(section);
        }
    }

    /**
             * 后处理：检测菜品列表中包含区域关键词的项（如"小碗菜"），
             * 将其拆分为独立区域，避免分类标题被当作菜品要求拍照。
             * 关键词在第一位时跳过（保留原区域名和价格），在中间时拆分出新区域。
             */
            private List<ParseResult.SectionResult> splitEmbeddedSections(
                    List<ParseResult.SectionResult> sections) {
                List<ParseResult.SectionResult> result = new ArrayList<>();
                for (ParseResult.SectionResult section : sections) {
                    List<String> items = section.getItems();
                    if (items == null || items.isEmpty()) {
                result.add(section);
                continue;
            }

            List<String> buf = new ArrayList<>();
            ParseResult.SectionResult cur = section;

            for (String item : items) {
                if (XIAOWAN_TITLE.matcher(item).matches()) {
                    if (!buf.isEmpty()) {
                        // 关键词在中间：拆分，前面菜品属于原区域，关键词开启新区块
                        cur.setItems(new ArrayList<>(buf));
                        result.add(cur);
                        buf.clear();
                        cur = new ParseResult.SectionResult();
                        cur.setName(item);
                        cur.setNeedsConfirmation(true);
                        cur.setXiaowan(true);
                    }
                    // 关键词在第一位：跳过（保留原区域名和价格信息）
                } else {
                    buf.add(item);
                }
            }
            cur.setItems(buf);
            result.add(cur);
        }
        return result;
    }

    /**
     * 后处理：将无菜品的标签区域（如独立的"小碗菜"行）合并到下一个区域名中，
     * 避免空区域卡片上出现"添加菜品"按钮。
     */
    private List<ParseResult.SectionResult> mergeLabelSections(
            List<ParseResult.SectionResult> sections) {
        List<ParseResult.SectionResult> result = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            ParseResult.SectionResult sec = sections.get(i);
            boolean isEmpty = sec.getItems() == null || sec.getItems().isEmpty();
            if (isEmpty && i + 1 < sections.size()) {
                ParseResult.SectionResult next = sections.get(i + 1);
                next.setName(sec.getName() + " · " + next.getName());
            } else {
                result.add(sec);
            }
        }
        return result;
    }

    // ---- 常量 ----

    private static final List<String> NON_PRICE_SECTIONS = List.of(
            "主食", "汤", "汤品", "小菜", "凉菜", "荤菜", "素菜",
            "甜点", "饮品", "饮料", "面食", "盖浇", "盖饭"
    );

    // ---- 内部类 ----

    private static class SectionMatch {
        String name;
        String priceText;
        BigDecimal price;
        String remaining;
        boolean needsConfirmation;

        SectionMatch(String name, String priceText, BigDecimal price,
                     String remaining, boolean needsConfirmation) {
            this.name = name;
            this.priceText = priceText;
            this.price = price;
            this.remaining = remaining;
            this.needsConfirmation = needsConfirmation;
        }
    }
}
