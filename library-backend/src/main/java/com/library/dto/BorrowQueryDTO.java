package com.library.dto;

import lombok.Data;

@Data
public class BorrowQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String status;
    private String keyword;
}
