package com.modeai.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "问题不能为空")
    private String message;

    private String conversationId;
}
