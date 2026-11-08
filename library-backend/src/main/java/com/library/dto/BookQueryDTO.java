package com.library.dto;

import lombok.Data;

@Data
public class BookQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Long categoryId;
    private Integer status; // 图书状态：1-上架，0-下架
}
