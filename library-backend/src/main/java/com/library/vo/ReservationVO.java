package com.library.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationVO {
    private Long id;
    private Long userId;
    private Long bookId;
    private String status;
    
    private String reserveTime;
    private String availableTime;
    private String expireTime;
    private String remark;
    private String createTime;
    
    private String username;
    private String realName;
    
    private String bookName;
    private String author;
    private String coverImg;
    
    private Integer daysRemaining;
    private String statusText;
}
