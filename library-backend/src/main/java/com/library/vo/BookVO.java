package com.library.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookVO {
    private Long id;
    private String bookName;
    private String author;
    private String isbn;
    private Long categoryId;
    private String categoryName;
    private String publisher;
    private LocalDate publishDate;
    private BigDecimal price;
    private Integer stock;
    private Integer total;
    private String description;
    private String coverImg;
    private Integer status;
}
