package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.dto.RecipeDetailResponse;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuSectionMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExcelImportService {

    private final MenuRecipeMapper recipeMapper;
    private final MenuSectionMapper sectionMapper;
    private final MenuItemMapper itemMapper;
    private final PermissionService permissionService;

    public ExcelImportService(MenuRecipeMapper recipeMapper,
                               MenuSectionMapper sectionMapper,
                               MenuItemMapper itemMapper,
                               PermissionService permissionService) {
        this.recipeMapper = recipeMapper;
        this.sectionMapper = sectionMapper;
        this.itemMapper = itemMapper;
        this.permissionService = permissionService;
    }

    @Transactional
    public RecipeDetailResponse importExcel(MultipartFile file, String title,
                                             LocalDate recipeDate, String canteenName,
                                             Long groupId, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }

        List<ParsedSection> parsedSections;
        try (InputStream is = file.getInputStream()) {
            Workbook workbook;
            if (filename.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(is);
            } else {
                workbook = new HSSFWorkbook(is);
            }
            parsedSections = parseSheet(workbook.getSheetAt(0));
            workbook.close();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "Excel 文件解析失败: " + e.getMessage());
        }

        if (parsedSections.isEmpty()) {
            throw new BusinessException(400, "Excel 文件中未识别到菜品数据，请检查格式");
        }

        // 如果第一行看起来像标题，用作文本标题
        if (title == null && !parsedSections.isEmpty() && parsedSections.get(0).possibleTitle != null) {
            title = parsedSections.get(0).possibleTitle;
        }

        MenuRecipe recipe = new MenuRecipe();
        recipe.setTitle(title != null ? title : "今日菜谱");
        recipe.setRecipeDate(recipeDate != null ? recipeDate : LocalDate.now());
        recipe.setCanteenName(canteenName);
        recipe.setGroupId(groupId);
        recipe.setCreatorId(userId);
        recipeMapper.insert(recipe);

        int sortOrder = 0;
        for (ParsedSection ps : parsedSections) {
            if (ps.name == null || ps.name.isBlank()) continue;
            MenuSection section = new MenuSection();
            section.setRecipeId(recipe.getId());
            section.setName(ps.name.trim());
            section.setPriceText(ps.priceText);
            section.setSortOrder(sortOrder++);
            sectionMapper.insert(section);

            int itemOrder = 0;
            for (String itemName : ps.items) {
                if (itemName == null || itemName.isBlank()) continue;
                MenuItem item = new MenuItem();
                item.setSectionId(section.getId());
                item.setRecipeId(recipe.getId());
                item.setName(itemName.trim());
                item.setSortOrder(itemOrder++);
                itemMapper.insert(item);
            }
        }

        return buildDetail(recipe);
    }

    private List<ParsedSection> parseSheet(Sheet sheet) {
        List<ParsedSection> sections = new ArrayList<>();
        String possibleTitle = null;

        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();

        for (int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            if (isRowEmpty(row)) continue;

            String colA = getCellString(row, 0);
            String colB = getCellString(row, 1);
            String colC = getCellString(row, 2);

            // 检测标题行
            if (isTitleRow(colA, colB, colC)) {
                possibleTitle = colA.length() >= colB.length() ? colA : (colA + " " + colB).trim();
                continue;
            }

            // 检测表头行
            if (isHeaderRow(colA)) continue;

            // 解析为区域行
            ParsedSection section = new ParsedSection();
            section.name = colA;
            section.priceText = extractPrice(colB);
            section.items = new ArrayList<>();

            // 从 colC 开始收集菜品
            for (int j = 2; j <= row.getLastCellNum(); j++) {
                String cell = getCellString(row, j);
                if (cell != null && !cell.isBlank()) {
                    // 尝试按分隔符拆分
                    List<String> split = splitItems(cell);
                    section.items.addAll(split);
                }
            }

            if (section.name != null && !section.name.isBlank()) {
                sections.add(section);
            }
        }

        if (!sections.isEmpty() && possibleTitle != null) {
            sections.get(0).possibleTitle = possibleTitle;
        }

        return sections;
    }

    private boolean isTitleRow(String colA, String colB, String colC) {
        if (colA == null) return false;
        String combined = (colA + " " + (colB != null ? colB : "")).trim();
        return (combined.contains("菜谱") || combined.contains("菜单") || combined.contains("套餐"))
                && (colC == null || colC.isEmpty());
    }

    private boolean isHeaderRow(String colA) {
        if (colA == null) return false;
        return colA.contains("区域") || colA.contains("分类") || colA.equals("名称");
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i <= row.getLastCellNum(); i++) {
            String val = getCellString(row, i);
            if (val != null && !val.isBlank()) return false;
        }
        return true;
    }

    private String extractPrice(String text) {
        if (text == null) return null;
        text = text.trim();
        // 如果看起来像价格文字，返回
        if (text.matches(".*[\\d一二两三四五六七八九十百]+.*[元块]?.*") || text.contains("免费")) {
            return text;
        }
        // 如果第一个菜品列的内容可能是价格（在只有3列且colC有内容时）
        return null;
    }

    private List<String> splitItems(String cell) {
        List<String> result = new ArrayList<>();
        if (cell == null || cell.isBlank()) return result;
        // 按中英文逗号、顿号、空格拆分
        String[] parts = cell.split("[，,、\\s]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 1) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private String getCellString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                // 整数不显示小数点
                double v = cell.getNumericCellValue();
                if (v == Math.floor(v) && !Double.isInfinite(v)) {
                    yield String.valueOf((long) v);
                }
                yield String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private RecipeDetailResponse buildDetail(MenuRecipe recipe) {
        RecipeDetailResponse dto = new RecipeDetailResponse();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setRecipeDate(recipe.getRecipeDate());
        dto.setCanteenName(recipe.getCanteenName());
        dto.setStatus(recipe.getStatus());

        List<MenuSection> sections = sectionMapper.selectByRecipeId(recipe.getId());
        List<MenuItem> allItems = itemMapper.selectByRecipeId(recipe.getId());
        Map<Long, List<MenuItem>> itemsBySection = new HashMap<>();
        for (MenuItem item : allItems) {
            itemsBySection.computeIfAbsent(item.getSectionId(), k -> new ArrayList<>()).add(item);
        }

        List<RecipeDetailResponse.SectionDto> sectionDtos = new ArrayList<>();
        for (MenuSection sec : sections) {
            RecipeDetailResponse.SectionDto sd = new RecipeDetailResponse.SectionDto();
            sd.setId(sec.getId());
            sd.setRecipeId(sec.getRecipeId());
            sd.setName(sec.getName());
            sd.setPriceText(sec.getPriceText());
            sd.setPrice(sec.getPrice());
            sd.setSortOrder(sec.getSortOrder());

            List<MenuItem> secItems = itemsBySection.getOrDefault(sec.getId(), List.of());
            List<RecipeDetailResponse.ItemDto> itemDtos = secItems.stream().map(item -> {
                RecipeDetailResponse.ItemDto idto = new RecipeDetailResponse.ItemDto();
                idto.setId(item.getId());
                idto.setSectionId(item.getSectionId());
                idto.setRecipeId(item.getRecipeId());
                idto.setName(item.getName());
                idto.setSortOrder(item.getSortOrder());
                idto.setImageStatus(item.getImageStatus());
                return idto;
            }).toList();
            sd.setItems(itemDtos);
            sectionDtos.add(sd);
        }
        dto.setSections(sectionDtos);
        return dto;
    }

    private static class ParsedSection {
        String name;
        String priceText;
        List<String> items;
        String possibleTitle;
    }
}
