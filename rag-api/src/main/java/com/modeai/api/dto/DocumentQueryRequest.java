package com.modeai.api.dto;

import lombok.Data;

@Data
public class DocumentQueryRequest {
    private String keyword;
    private String status;
    private Long departmentId;
    private Integer page = 1;
    private Integer size = 10;
}
