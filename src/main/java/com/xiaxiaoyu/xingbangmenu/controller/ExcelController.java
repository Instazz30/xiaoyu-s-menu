package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.dto.RecipeDetailResponse;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.ExcelImportService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/recipes")
public class ExcelController {

    private final ExcelImportService excelImportService;
    private final PermissionService permissionService;

    public ExcelController(ExcelImportService excelImportService, PermissionService permissionService) {
        this.excelImportService = excelImportService;
        this.permissionService = permissionService;
    }

    /** Excel 批量导入菜谱 */
    @PostMapping("/import")
    public Result<RecipeDetailResponse> importExcel(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long groupId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recipeDate,
            @RequestParam(required = false) String canteenName) {
        SysUser user = permissionService.currentUser(request);
        RecipeDetailResponse result =
                excelImportService.importExcel(file, title, recipeDate, canteenName, groupId, user.getId());
        return Result.ok(result);
    }
}
