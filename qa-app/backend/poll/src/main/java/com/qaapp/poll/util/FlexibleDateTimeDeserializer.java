package com.qaapp.poll.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class FlexibleDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    // 支持多种日期格式
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_DATE_TIME,                      // 2025-04-26T12:30:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), // 带毫秒的ISO格式
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),   // 2025-04-26 12:30:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd")             // 仅日期：2025-04-26
    );

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText().trim();
        
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        // 尝试各种格式解析
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // 如果是纯日期格式，则添加午夜时间
                if (formatter.equals(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    return LocalDateTime.of(date, LocalTime.MIDNIGHT);
                }
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
                continue;
            }
        }
        
        // 如果所有格式都无法解析，抛出异常
        throw new IllegalArgumentException("无法解析日期时间: " + dateStr);
    }
}