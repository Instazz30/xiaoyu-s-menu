package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.dto.InspectionUpdateRequest;
import com.xiaxiaoyu.xingbangmenu.entity.InspectionIssue;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.InspectionIssueMapper;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class InspectionService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_IMAGE_SIZE = 1920;
    private static final int THUMBNAIL_SIZE = 300;
    private static final DateTimeFormatter EXPORT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InspectionIssueMapper issueMapper;

    @Value("${upload.storage-path:./uploads}")
    private String storagePath;

    public InspectionService(InspectionIssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    @Transactional
    public InspectionIssue create(Long userId, MultipartFile file,
                                  String location, String reason, String measure) {
        String normalizedLocation = normalize(location, 255, "地点");
        String normalizedReason = normalize(reason, 1000, "原因");
        String normalizedMeasure = normalize(measure, 1000, "措施");
        StoredImage image = storeImage(userId, file);
        InspectionIssue issue = new InspectionIssue();
        issue.setUserId(userId);
        issue.setLocation(normalizedLocation);
        issue.setReason(normalizedReason);
        issue.setMeasure(normalizedMeasure);
        issue.setIssueImageUrl(image.originalUrl());
        issue.setIssueThumbnailUrl(image.thumbnailUrl());
        issue.setStatus("pending");
        issueMapper.insert(issue);
        return requireOwned(issue.getId(), userId);
    }

    public List<InspectionIssue> list(Long userId, LocalDate date, String location) {
        LocalDateTime start = date == null ? null : date.atStartOfDay();
        LocalDateTime end = date == null ? null : date.plusDays(1).atStartOfDay();
        return issueMapper.selectByUserId(userId, start, end, normalize(location, 255, "地点"));
    }

    public InspectionIssue detail(Long id, Long userId) {
        return requireOwned(id, userId);
    }

    @Transactional
    public InspectionIssue update(Long id, Long userId, InspectionUpdateRequest request) {
        requireOwned(id, userId);
        InspectionIssue issue = new InspectionIssue();
        issue.setId(id);
        issue.setUserId(userId);
        issue.setLocation(normalize(request.getLocation(), 255, "地点"));
        issue.setReason(normalize(request.getReason(), 1000, "原因"));
        issue.setMeasure(normalize(request.getMeasure(), 1000, "措施"));
        if (issueMapper.updateText(issue) == 0) {
            throw new BusinessException(404, "隐患记录不存在");
        }
        return requireOwned(id, userId);
    }

    @Transactional
    public InspectionIssue uploadResultImage(Long id, Long userId, MultipartFile file) {
        requireOwned(id, userId);
        StoredImage image = storeImage(userId, file);
        if (issueMapper.updateResultImage(id, userId, image.originalUrl(), image.thumbnailUrl(), "rectified") == 0) {
            throw new BusinessException(404, "隐患记录不存在");
        }
        return requireOwned(id, userId);
    }

    @Transactional
    public InspectionIssue removeResultImage(Long id, Long userId) {
        requireOwned(id, userId);
        if (issueMapper.updateResultImage(id, userId, null, null, "pending") == 0) {
            throw new BusinessException(404, "隐患记录不存在");
        }
        return requireOwned(id, userId);
    }

    @Transactional
    public void deleteBatch(Long userId, List<Long> requestedIds) {
        List<Long> ids = normalizeIds(requestedIds);
        List<InspectionIssue> issues = issueMapper.selectByIds(userId, ids);
        if (issues.size() != ids.size()) {
            throw new BusinessException(403, "部分记录不存在或不属于当前用户");
        }
        if (issueMapper.deleteByIds(userId, ids) != ids.size()) {
            throw new BusinessException(500, "批量删除失败，请刷新后重试");
        }
    }

    public byte[] exportXlsx(Long userId, List<Long> requestedIds) {
        List<Long> ids = normalizeIds(requestedIds);
        List<InspectionIssue> issues = issueMapper.selectByIds(userId, ids);
        if (issues.size() != ids.size()) {
            throw new BusinessException(403, "部分记录不存在或不属于当前用户");
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("隐患检查");
            String[] headers = {"时间", "地点", "原因", "图片", "措施", "结果图片"};
            int[] widths = {19, 22, 30, 24, 30, 24};

            CellStyle headerStyle = headerStyle(workbook);
            CellStyle textStyle = textStyle(workbook);
            Row header = sheet.createRow(0);
            header.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths[i] * 256);
            }

            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            CreationHelper helper = workbook.getCreationHelper();
            int rowIndex = 1;
            for (InspectionIssue issue : issues) {
                Row row = sheet.createRow(rowIndex);
                row.setHeightInPoints(92);
                setText(row, 0, issue.getCreatedAt() == null ? "" : issue.getCreatedAt().format(EXPORT_TIME), textStyle);
                setText(row, 1, safe(issue.getLocation()), textStyle);
                setText(row, 2, safe(issue.getReason()), textStyle);
                setText(row, 3, "", textStyle);
                setText(row, 4, safe(issue.getMeasure()), textStyle);
                setText(row, 5, "", textStyle);
                addImage(workbook, drawing, helper, issue.getIssueImageUrl(), 3, rowIndex);
                if (issue.getResultImageUrl() != null && !issue.getResultImageUrl().isBlank()) {
                    addImage(workbook, drawing, helper, issue.getResultImageUrl(), 5, rowIndex);
                }
                rowIndex++;
            }
            sheet.createFreezePane(0, 1);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "导出失败: " + e.getMessage());
        }
    }

    private InspectionIssue requireOwned(Long id, Long userId) {
        InspectionIssue issue = issueMapper.selectById(id);
        if (issue == null) {
            throw new BusinessException(404, "隐患记录不存在");
        }
        if (!userId.equals(issue.getUserId())) {
            throw new BusinessException(403, "无权访问该隐患记录");
        }
        return issue;
    }

    private StoredImage storeImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传图片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "图片大小不能超过10MB");
        }

        try {
            BufferedImage source = ImageIO.read(file.getInputStream());
            if (source == null) {
                throw new BusinessException(400, "仅支持常见图片格式");
            }
            Path root = Paths.get(storagePath).toAbsolutePath().normalize();
            Path directory = root.resolve("inspections").resolve(userId.toString()).normalize();
            if (!directory.startsWith(root)) {
                throw new BusinessException(500, "图片存储路径异常");
            }
            Files.createDirectories(directory);
            String baseName = UUID.randomUUID().toString();
            Path original = directory.resolve(baseName + ".jpg");
            Path thumbnail = directory.resolve(baseName + "_thumb.jpg");
            Thumbnails.of(source).size(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE).outputFormat("jpg").toFile(original.toFile());
            Thumbnails.of(source).size(THUMBNAIL_SIZE, THUMBNAIL_SIZE).outputFormat("jpg").toFile(thumbnail.toFile());
            String urlBase = "/uploads/inspections/" + userId + "/" + baseName;
            return new StoredImage(urlBase + ".jpg", urlBase + "_thumb.jpg");
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(500, "图片处理失败: " + e.getMessage());
        }
    }

    private List<Long> normalizeIds(List<Long> requestedIds) {
        if (requestedIds == null || requestedIds.isEmpty()) {
            throw new BusinessException(400, "请至少选择一条记录");
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long id : requestedIds) {
            if (id != null && id > 0) unique.add(id);
        }
        if (unique.isEmpty()) {
            throw new BusinessException(400, "请至少选择一条记录");
        }
        return new ArrayList<>(unique);
    }

    private String normalize(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new BusinessException(400, fieldName + "不能超过" + maxLength + "个字符");
        }
        return normalized;
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorders(style);
        return style;
    }

    private CellStyle textStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.LEFT);
        applyBorders(style);
        return style;
    }

    private void applyBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private void setText(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addImage(XSSFWorkbook workbook, XSSFDrawing drawing, CreationHelper helper,
                          String url, int column, int row) throws IOException {
        Path path = resolveImagePath(url);
        if (!Files.isRegularFile(path)) {
            throw new BusinessException(500, "导出失败，图片文件不存在");
        }
        int pictureIndex = workbook.addPicture(Files.readAllBytes(path), Workbook.PICTURE_TYPE_JPEG);
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(column);
        anchor.setCol2(column + 1);
        anchor.setRow1(row);
        anchor.setRow2(row + 1);
        anchor.setDx1(8 * 9525);
        anchor.setDy1(6 * 9525);
        anchor.setDx2(0);
        anchor.setDy2(0);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        drawing.createPicture(anchor, pictureIndex);
    }

    private Path resolveImagePath(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            throw new BusinessException(500, "图片地址异常");
        }
        Path root = Paths.get(storagePath).toAbsolutePath().normalize();
        Path path = root.resolve(url.substring("/uploads/".length())).normalize();
        if (!path.startsWith(root)) {
            throw new BusinessException(500, "图片地址异常");
        }
        return path;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record StoredImage(String originalUrl, String thumbnailUrl) {}
}
