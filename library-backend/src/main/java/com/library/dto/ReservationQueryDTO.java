package com.library.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String status;
}
