package com.qaapp.poll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.qaapp.poll.util.FlexibleDateTimeDeserializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePollRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    private String description;

    @JsonDeserialize(using = FlexibleDateTimeDeserializer.class)
    private LocalDateTime startDate;
    
    // 使用灵活的日期时间反序列化器
    @JsonDeserialize(using = FlexibleDateTimeDeserializer.class)
    private LocalDateTime endDate;

    @JsonProperty("isActive")
    private boolean isActive = true;    
    
    @NotEmpty(message = "至少需要两个选项")
    @Size(min = 2, message = "至少需要两个选项")
    private List<CreateOptionRequest> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOptionRequest {
        @NotBlank(message = "选项内容不能为空")
        private String content;
    }
}