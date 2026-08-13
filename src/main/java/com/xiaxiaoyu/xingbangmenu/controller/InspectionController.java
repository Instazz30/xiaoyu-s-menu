package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.dto.InspectionExportRequest;
import com.xiaxiaoyu.xingbangmenu.dto.InspectionUpdateRequest;
import com.xiaxiaoyu.xingbangmenu.entity.InspectionIssue;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.InspectionService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inspections")
public class InspectionController {

    private final InspectionService inspectionService;
    private final PermissionService permissionService;

    public InspectionController(InspectionService inspectionService,
                                PermissionService permissionService) {
        this.inspectionService = inspectionService;
        this.permissionService = permissionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<InspectionIssue> create(HttpServletRequest request,
                                          @RequestParam("file") MultipartFile file,
                                          @RequestParam(required = false) String location,
                                          @RequestParam(required = false) String reason,
                                          @RequestParam(required = false) String measure) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(inspectionService.create(user.getId(), file, location, reason, measure));
    }

    @GetMapping
    public Result<List<InspectionIssue>> list(
            HttpServletRequest request,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String location) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(inspectionService.list(user.getId(), date, location));
    }

    @GetMapping("/{id}")
    public Result<InspectionIssue> detail(HttpServletRequest request, @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(inspectionService.detail(id, user.getId()));
    }

    @PutMapping("/{id}")
    public Result<InspectionIssue> update(HttpServletRequest request,
                                          @PathVariable Long id,
                                          @RequestBody(required = false) InspectionUpdateRequest body) {
        SysUser user = permissionService.currentUser(request);
        if (body == null) body = new InspectionUpdateRequest();
        return Result.ok(inspectionService.update(id, user.getId(), body));
    }

    @PostMapping(value = "/{id}/result-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<InspectionIssue> uploadResultImage(HttpServletRequest request,
                                                     @PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(inspectionService.uploadResultImage(id, user.getId(), file));
    }

    @DeleteMapping("/{id}/result-image")
    public Result<InspectionIssue> removeResultImage(HttpServletRequest request,
                                                     @PathVariable Long id) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(inspectionService.removeResultImage(id, user.getId()));
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(HttpServletRequest request,
                                    @RequestBody InspectionExportRequest body) {
        SysUser user = permissionService.currentUser(request);
        inspectionService.deleteBatch(user.getId(), body.getIds());
        return Result.ok();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(HttpServletRequest request,
                                         @RequestBody InspectionExportRequest body) {
        SysUser user = permissionService.currentUser(request);
        byte[] data = inspectionService.exportXlsx(user.getId(), body.getIds());
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String filename = URLEncoder.encode("隐患检查_" + time + ".xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
