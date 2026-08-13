package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.entity.InspectionIssue;
import com.xiaxiaoyu.xingbangmenu.mapper.InspectionIssueMapper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InspectionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldExportSelectedIssueWithEmbeddedImage() throws Exception {
        InspectionIssueMapper mapper = mock(InspectionIssueMapper.class);
        InspectionService service = new InspectionService(mapper);
        ReflectionTestUtils.setField(service, "storagePath", tempDir.toString());

        Path image = tempDir.resolve("inspections/7/problem.jpg");
        Files.createDirectories(image.getParent());
        BufferedImage source = new BufferedImage(120, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, 120, 80);
        graphics.dispose();
        ImageIO.write(source, "jpg", image.toFile());

        InspectionIssue issue = new InspectionIssue();
        issue.setId(11L);
        issue.setUserId(7L);
        issue.setLocation("配电间");
        issue.setReason("电线裸露");
        issue.setMeasure("增加绝缘保护");
        issue.setIssueImageUrl("/uploads/inspections/7/problem.jpg");
        issue.setStatus("pending");
        issue.setCreatedAt(LocalDateTime.of(2026, 8, 7, 14, 30));
        when(mapper.selectByIds(eq(7L), anyList())).thenReturn(List.of(issue));

        byte[] data = service.exportXlsx(7L, List.of(11L));

        assertTrue(data.length > 0);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(data))) {
            var sheet = workbook.getSheet("隐患检查");
            assertEquals("时间", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("结果图片", sheet.getRow(0).getCell(5).getStringCellValue());
            assertEquals("2026-08-07 14:30", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("配电间", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals(1, workbook.getAllPictures().size());
        }
    }

    @Test
    void shouldFilterByDateAndLocation() {
        InspectionIssueMapper mapper = mock(InspectionIssueMapper.class);
        InspectionService service = new InspectionService(mapper);
        LocalDate date = LocalDate.of(2026, 8, 7);
        when(mapper.selectByUserId(
                eq(7L), eq(date.atStartOfDay()), eq(date.plusDays(1).atStartOfDay()), eq("食堂")))
                .thenReturn(List.of());

        service.list(7L, date, " 食堂 ");

        verify(mapper).selectByUserId(
                7L, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), "食堂");
    }

    @Test
    void shouldBatchDeleteOwnedIssues() {
        InspectionIssueMapper mapper = mock(InspectionIssueMapper.class);
        InspectionService service = new InspectionService(mapper);
        InspectionIssue issue = new InspectionIssue();
        issue.setId(11L);
        issue.setUserId(7L);
        when(mapper.selectByIds(eq(7L), anyList())).thenReturn(List.of(issue));
        when(mapper.deleteByIds(eq(7L), anyList())).thenReturn(1);

        service.deleteBatch(7L, List.of(11L));

        verify(mapper).deleteByIds(7L, List.of(11L));
    }
}
