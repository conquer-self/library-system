package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.Result;
import com.library.dto.ReservationQueryDTO;
import com.library.service.ReservationService;
import com.library.vo.ReservationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping("/reserve")
    public Result<Void> reserveBook(@RequestAttribute("userId") Long userId,
                                    @RequestBody Map<String, Long> params) {
        Long bookId = params.get("bookId");
        reservationService.reserveBook(userId, bookId);
        return Result.success("预约成功", null);
    }

    @PostMapping("/confirm/{id}")
    public Result<Void> confirmBorrow(@RequestAttribute("userId") Long userId,
                                      @PathVariable Long id) {
        reservationService.confirmBorrow(userId, id);
        return Result.success("确认借阅成功", null);
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancelReservation(@RequestAttribute("userId") Long userId,
                                          @PathVariable Long id) {
        reservationService.cancelReservation(userId, id);
        return Result.success("取消预约成功", null);
    }

    @GetMapping("/my")
    public Result<IPage<ReservationVO>> getMyReservationList(@RequestAttribute("userId") Long userId,
                                                             ReservationQueryDTO queryDTO) {
        IPage<ReservationVO> page = reservationService.getMyReservationList(userId, queryDTO);
        return Result.success(page);
    }

    @GetMapping("/list")
    public Result<IPage<ReservationVO>> getAllReservationList(ReservationQueryDTO queryDTO) {
        IPage<ReservationVO> page = reservationService.getAllReservationList(queryDTO);
        return Result.success(page);
    }

    @PostMapping("/admin/cancel/{id}")
    public Result<Void> adminCancelReservation(@PathVariable Long id,
                                               @RequestParam(required = false) String remark) {
        reservationService.adminCancelReservation(id, remark);
        return Result.success("取消预约成功", null);
    }
}
