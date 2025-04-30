package com.qaapp.poll.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
    
    private final String[] formatPatterns;
    
    public FlexibleLocalDateTimeDeserializer(String... formatPatterns) {
        super(DateTimeFormatter.ofPattern(formatPatterns[0]));
        this.formatPatterns = formatPatterns;
    }
    
    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String dateStr = parser.getText().trim();
        if (dateStr.isEmpty()) {
            return null;
        }
        
        // Try each pattern until one works
        for (String pattern : formatPatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Continue to next pattern
            }
        }
        
        // If we get here, none of the patterns worked
        throw new IOException("Cannot parse date: " + dateStr);
    }
}