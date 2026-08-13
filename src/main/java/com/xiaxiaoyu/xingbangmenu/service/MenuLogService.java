package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.common.PageResult;
import com.xiaxiaoyu.xingbangmenu.entity.GroupMember;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import com.xiaxiaoyu.xingbangmenu.entity.MenuItemLog;
import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.GroupMemberMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuItemLogMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.SysUserMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuLogService {

    private final MenuItemLogMapper logMapper;
    private final GroupMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final PermissionService permissionService;

    public MenuLogService(MenuItemLogMapper logMapper,
                          GroupMemberMapper memberMapper,
                          SysUserMapper userMapper,
                          PermissionService permissionService) {
        this.logMapper = logMapper;
        this.memberMapper = memberMapper;
        this.userMapper = userMapper;
        this.permissionService = permissionService;
    }

    /** 记录一次菜品信息操作（新增/修改/删除），由系统自动生成 */
    public void record(MenuRecipe recipe, MenuItem item, Long userId,
                       String actionType, String fieldName, String oldValue, String newValue) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return;
        GroupMember member = memberMapper.selectByGroupAndUser(recipe.getGroupId(), userId);

        MenuItemLog log = new MenuItemLog();
        log.setGroupId(recipe.getGroupId());
        log.setRecipeId(recipe.getId());
        log.setRecipeDate(recipe.getRecipeDate());
        log.setItemId(item != null ? item.getId() : null);
        log.setItemName(item != null && item.getName() != null ? item.getName() : "");
        log.setUserId(userId);
        log.setUserName(user.getNickname());
        log.setRole(member != null ? member.getRole() : "member");
        log.setActionType(actionType);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        logMapper.insert(log);
    }

    public PageResult<Map<String, Object>> list(Long groupId, LocalDate menuDate, Long operatorId,
                                                String keyword, int page, int pageSize, Long userId) {
        permissionService.requireMember(groupId, userId);
        int offset = (page - 1) * pageSize;
        List<MenuItemLog> logs = logMapper.selectByGroupId(groupId, menuDate, operatorId, keyword, offset, pageSize);
        long total = logMapper.countByGroupId(groupId, menuDate, operatorId, keyword);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MenuItemLog l : logs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", l.getId());
            item.put("groupId", l.getGroupId());
            item.put("recipeId", l.getRecipeId());
            item.put("recipeDate", l.getRecipeDate() == null ? null : l.getRecipeDate().toString());
            item.put("itemId", l.getItemId());
            item.put("itemName", l.getItemName());
            item.put("userId", l.getUserId());
            item.put("userName", l.getUserName());
            item.put("role", l.getRole());
            item.put("actionType", l.getActionType());
            item.put("fieldName", l.getFieldName());
            item.put("oldValue", l.getOldValue());
            item.put("newValue", l.getNewValue());
            item.put("createdAt", l.getCreatedAt() == null ? null : l.getCreatedAt().toString());
            list.add(item);
        }
        return new PageResult<>(list, total, page, pageSize);
    }

    /** 导出日期区间内的修改记录为 xlsx（仅管理员） */
    public byte[] exportXlsx(Long groupId, LocalDate dateFrom, LocalDate dateTo, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        List<MenuItemLog> logs = logMapper.selectAllForExport(groupId, dateFrom, dateTo);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("修改记录");
            String[] headers = {"序号", "修改时间", "操作人", "角色", "菜单日期",
                    "操作类型", "修改字段", "修改前菜名", "修改后菜名"};

            CellStyle headStyle = workbook.createCellStyle();
            Font headFont = workbook.createFont();
            headFont.setBold(true);
            headStyle.setFont(headFont);

            Row headRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowIdx = 1;
            for (MenuItemLog log : logs) {
                String beforeName;
                String afterName;
                if ("create".equals(log.getActionType())) {
                    beforeName = "";
                    afterName = safe(log.getItemName());
                } else if ("delete".equals(log.getActionType())) {
                    beforeName = safe(log.getItemName());
                    afterName = "";
                } else if ("name".equals(log.getFieldName())) {
                    beforeName = safe(log.getOldValue());
                    afterName = safe(log.getNewValue());
                } else {
                    beforeName = safe(log.getItemName());
                    afterName = safe(log.getItemName());
                }
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowIdx);
                row.createCell(1).setCellValue(
                        log.getCreatedAt() != null ? log.getCreatedAt().format(dtf) : "");
                row.createCell(2).setCellValue(safe(log.getUserName()));
                row.createCell(3).setCellValue("admin".equals(log.getRole()) ? "管理员" : "成员");
                row.createCell(4).setCellValue(
                        log.getRecipeDate() != null ? log.getRecipeDate().toString() : "");
                row.createCell(5).setCellValue(actionLabel(log.getActionType()));
                row.createCell(6).setCellValue(fieldLabel(log.getFieldName()));
                row.createCell(7).setCellValue(beforeName);
                row.createCell(8).setCellValue(afterName);
                rowIdx++;
            }

            int[] widths = {6, 20, 12, 8, 12, 8, 10, 22, 22};
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "导出失败: " + e.getMessage());
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String actionLabel(String action) {
        if ("create".equals(action)) return "新增";
        if ("delete".equals(action)) return "删除";
        return "修改";
    }

    private String fieldLabel(String field) {
        if (field == null) return "";
        return switch (field) {
            case "name" -> "名称";
            case "description" -> "描述";
            case "section" -> "分类";
            case "sortOrder" -> "排序";
            default -> field;
        };
    }
}
