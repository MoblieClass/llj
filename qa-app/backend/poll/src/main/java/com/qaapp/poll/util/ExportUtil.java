package com.qaapp.poll.util;

import com.qaapp.poll.dto.PollDTO;
import com.qaapp.poll.dto.PollOptionDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class ExportUtil {

    // 导出为CSV
    public void exportPollToCsv(PollDTO poll, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + 
                           sanitizeFileName(poll.getTitle()) + "_poll_results.csv\"");
        
        // 添加BOM标记，确保Excel正确显示中文
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        
        StringBuilder csvContent = new StringBuilder();
        
        // 标题行
        csvContent.append("投票标题:,").append(poll.getTitle()).append("\n");
        if (poll.getDescription() != null && !poll.getDescription().isEmpty()) {
            csvContent.append("描述:,").append(poll.getDescription()).append("\n");
        }
        csvContent.append("创建日期:,").append(formatDateTime(poll.getCreatedAt())).append("\n");
        if (poll.getEndDate() != null) {
            csvContent.append("结束日期:,").append(formatDateTime(poll.getEndDate())).append("\n");
        }
        csvContent.append("状态:,").append(poll.isActive() ? "进行中" : "已结束").append("\n\n");
        
        // 表头
        csvContent.append("选项,票数,百分比\n");
        
        // 计算总票数
        int totalVotes = getTotalVotes(poll.getOptions());
        
        // 数据行
        for (PollOptionDTO option : poll.getOptions()) {
            String percentage = totalVotes > 0 ? String.format("%.1f%%", (double) option.getVotes() / totalVotes * 100) : "0.0%";
            csvContent.append(option.getContent()).append(",")
                      .append(option.getVotes()).append(",")
                      .append(percentage).append("\n");
        }
        
        // 总计行
        csvContent.append("总计,").append(totalVotes).append(",100%\n");
        
        response.getWriter().write(csvContent.toString());
        response.getWriter().flush();
    }
    
    // 导出为Excel
    public void exportPollToExcel(PollDTO poll, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + 
                           sanitizeFileName(poll.getTitle()) + "_poll_results.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("投票结果");
            
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);
            
            int rowNum = 0;
            
            // 标题信息
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleLabelCell = titleRow.createCell(0);
            titleLabelCell.setCellValue("投票标题:");
            titleLabelCell.setCellStyle(headerStyle);
            Cell titleValueCell = titleRow.createCell(1);
            titleValueCell.setCellValue(poll.getTitle());
            
            if (poll.getDescription() != null && !poll.getDescription().isEmpty()) {
                Row descRow = sheet.createRow(rowNum++);
                Cell descLabelCell = descRow.createCell(0);
                descLabelCell.setCellValue("描述:");
                descLabelCell.setCellStyle(headerStyle);
                Cell descValueCell = descRow.createCell(1);
                descValueCell.setCellValue(poll.getDescription());
            }
            
            Row createdRow = sheet.createRow(rowNum++);
            Cell createdLabelCell = createdRow.createCell(0);
            createdLabelCell.setCellValue("创建日期:");
            createdLabelCell.setCellStyle(headerStyle);
            Cell createdValueCell = createdRow.createCell(1);
            createdValueCell.setCellValue(formatDateTime(poll.getCreatedAt()));
            
            if (poll.getEndDate() != null) {
                Row endRow = sheet.createRow(rowNum++);
                Cell endLabelCell = endRow.createCell(0);
                endLabelCell.setCellValue("结束日期:");
                endLabelCell.setCellStyle(headerStyle);
                Cell endValueCell = endRow.createCell(1);
                endValueCell.setCellValue(formatDateTime(poll.getEndDate()));
            }
            
            Row statusRow = sheet.createRow(rowNum++);
            Cell statusLabelCell = statusRow.createCell(0);
            statusLabelCell.setCellValue("状态:");
            statusLabelCell.setCellStyle(headerStyle);
            Cell statusValueCell = statusRow.createCell(1);
            statusValueCell.setCellValue(poll.isActive() ? "进行中" : "已结束");
            
            // 空行
            rowNum++;
            
            // 表头
            Row headerRow = sheet.createRow(rowNum++);
            Cell optionHeaderCell = headerRow.createCell(0);
            optionHeaderCell.setCellValue("选项");
            optionHeaderCell.setCellStyle(headerStyle);
            Cell votesHeaderCell = headerRow.createCell(1);
            votesHeaderCell.setCellValue("票数");
            votesHeaderCell.setCellStyle(headerStyle);
            Cell percentHeaderCell = headerRow.createCell(2);
            percentHeaderCell.setCellValue("百分比");
            percentHeaderCell.setCellStyle(headerStyle);
            
            // 计算总票数
            int totalVotes = getTotalVotes(poll.getOptions());
            
            // 数据行
            for (PollOptionDTO option : poll.getOptions()) {
                Row dataRow = sheet.createRow(rowNum++);
                Cell optionCell = dataRow.createCell(0);
                optionCell.setCellValue(option.getContent());
                optionCell.setCellStyle(dataStyle);
                
                Cell votesCell = dataRow.createCell(1);
                votesCell.setCellValue(option.getVotes());
                votesCell.setCellStyle(dataStyle);
                
                Cell percentCell = dataRow.createCell(2);
                String percentage = totalVotes > 0 ? String.format("%.1f%%", (double) option.getVotes() / totalVotes * 100) : "0.0%";
                percentCell.setCellValue(percentage);
                percentCell.setCellStyle(dataStyle);
            }
            
            // 总计行
            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("总计");
            totalLabelCell.setCellStyle(totalStyle);
            
            Cell totalVotesCell = totalRow.createCell(1);
            totalVotesCell.setCellValue(totalVotes);
            totalVotesCell.setCellStyle(totalStyle);
            
            Cell totalPercentCell = totalRow.createCell(2);
            totalPercentCell.setCellValue("100.0%");
            totalPercentCell.setCellStyle(totalStyle);
            
            // 自动调整列宽
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(response.getOutputStream());
        }
    }
    
    // 创建表头样式
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    // 创建数据样式
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    // 创建总计行样式
    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    // 获取总票数
    private int getTotalVotes(List<PollOptionDTO> options) {
        return options.stream().mapToInt(PollOptionDTO::getVotes).sum();
    }
    
    // 格式化日期时间
    private String formatDateTime(Object dateTime) {
        if (dateTime == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (dateTime instanceof Date) {
            return sdf.format((Date) dateTime);
        } else {
            return dateTime.toString();
        }
    }
    
    // 处理文件名
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}