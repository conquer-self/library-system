package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.dto.ReservationQueryDTO;
import com.library.vo.ReservationVO;

public interface ReservationService {
    
    IPage<ReservationVO> getMyReservationList(Long userId, ReservationQueryDTO queryDTO);
    
    IPage<ReservationVO> getAllReservationList(ReservationQueryDTO queryDTO);
    
    void reserveBook(Long userId, Long bookId);
    
    void confirmBorrow(Long userId, Long id);
    
    void cancelReservation(Long userId, Long id);
    
    void adminCancelReservation(Long id, String remark);
    
    void handleBookReturned(Long bookId);
}
