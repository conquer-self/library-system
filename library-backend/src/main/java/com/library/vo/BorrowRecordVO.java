package com.library.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BorrowRecordVO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long bookId;
    private String bookName;
    private String coverImg;
    private String author;
    private LocalDateTime borrowTime;
    private LocalDateTime dueTime;
    private LocalDateTime returnTime;
    private Integer renewCount;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
