package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.dto.ReservationQueryDTO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.ReservationMapper;
import com.library.mapper.UserMapper;
import com.library.service.ReservationService;
import com.library.vo.ReservationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    private static final int MAX_RESERVATION_COUNT = 3;
    private static final int RESERVATION_EXPIRE_DAYS = 3;
    private static final int MAX_BORROW_COUNT = 5;

    @Override
    public IPage<ReservationVO> getMyReservationList(Long userId, ReservationQueryDTO queryDTO) {
        Page<Reservation> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId);
        
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Reservation::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(Reservation::getCreateTime);
        
        IPage<Reservation> recordPage = this.page(page, wrapper);
        return recordPage.convert(this::convertToVO);
    }

    @Override
    public IPage<ReservationVO> getAllReservationList(ReservationQueryDTO queryDTO) {
        Page<Reservation> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Reservation::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(Reservation::getCreateTime);
        
        IPage<Reservation> recordPage = this.page(page, wrapper);
        return recordPage.convert(this::convertToVO);
    }

    @Override
    @Transactional
    public void reserveBook(Long userId, Long bookId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("您的账号已被禁用，无法预约");
        }

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }

        if (book.getStock() > 0) {
            throw new BusinessException("该图书有库存，无需预约");
        }

        LambdaQueryWrapper<Reservation> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Reservation::getUserId, userId)
                   .in(Reservation::getStatus, "waiting", "available");
        long reservationCount = this.count(countWrapper);
        if (reservationCount >= MAX_RESERVATION_COUNT) {
            throw new BusinessException("您最多只能预约" + MAX_RESERVATION_COUNT + "本图书");
        }

        LambdaQueryWrapper<Reservation> repeatWrapper = new LambdaQueryWrapper<>();
        repeatWrapper.eq(Reservation::getUserId, userId)
                    .eq(Reservation::getBookId, bookId)
                    .in(Reservation::getStatus, "waiting", "available", "converted");
        if (this.count(repeatWrapper) > 0) {
            throw new BusinessException("您已预约或正在借阅该图书");
        }

        LambdaQueryWrapper<BorrowRecord> borrowingWrapper = new LambdaQueryWrapper<>();
        borrowingWrapper.eq(BorrowRecord::getUserId, userId)
                       .eq(BorrowRecord::getBookId, bookId)
                       .in(BorrowRecord::getStatus, "borrowing", "returning");
        if (borrowRecordMapper.selectCount(borrowingWrapper) > 0) {
            throw new BusinessException("您正在借阅该图书");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setStatus("waiting");
        reservation.setReserveTime(LocalDateTime.now());
        this.save(reservation);
    }

    @Override
    @Transactional
    public void confirmBorrow(Long userId, Long id) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此预约记录");
        }

        if (!"available".equals(reservation.getStatus())) {
            throw new BusinessException("当前预约状态无法确认借阅");
        }

        if (reservation.getExpireTime() != null && reservation.getExpireTime().isBefore(LocalDateTime.now())) {
            reservation.setStatus("expired");
            this.updateById(reservation);
            throw new BusinessException("预约已过期，请重新预约");
        }

        LambdaQueryWrapper<BorrowRecord> borrowCountWrapper = new LambdaQueryWrapper<>();
        borrowCountWrapper.eq(BorrowRecord::getUserId, userId)
                         .in(BorrowRecord::getStatus, "pending", "borrowing", "returning");
        long borrowCount = borrowRecordMapper.selectCount(borrowCountWrapper);
        if (borrowCount >= MAX_BORROW_COUNT) {
            throw new BusinessException("您已达到最大借阅数量（" + MAX_BORROW_COUNT + "本）");
        }

        Book book = bookMapper.selectById(reservation.getBookId());
        if (book == null || book.getStock() < 0) {
            throw new BusinessException("图书状态异常，无法借阅");
        }

        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(userId);
        borrowRecord.setBookId(reservation.getBookId());
        borrowRecord.setStatus("pending");
        borrowRecord.setRenewCount(0);
        borrowRecordMapper.insert(borrowRecord);

        reservation.setStatus("converted");
        this.updateById(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, Long id) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此预约记录");
        }

        if (!"waiting".equals(reservation.getStatus()) && !"available".equals(reservation.getStatus())) {
            throw new BusinessException("当前预约状态无法取消");
        }

        reservation.setStatus("cancelled");
        this.updateById(reservation);
    }

    @Override
    @Transactional
    public void adminCancelReservation(Long id, String remark) {
        Reservation reservation = this.getById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!"waiting".equals(reservation.getStatus()) && !"available".equals(reservation.getStatus())) {
            throw new BusinessException("当前预约状态无法取消");
        }

        reservation.setStatus("cancelled");
        reservation.setRemark(remark);
        this.updateById(reservation);
    }

    @Override
    @Transactional
    public void handleBookReturned(Long bookId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getBookId, bookId)
               .eq(Reservation::getStatus, "waiting")
               .orderByAsc(Reservation::getReserveTime);
        
        List<Reservation> waitingList = this.list(wrapper);
        if (!waitingList.isEmpty()) {
            Reservation firstReservation = waitingList.get(0);
            firstReservation.setStatus("available");
            firstReservation.setAvailableTime(LocalDateTime.now());
            firstReservation.setExpireTime(LocalDateTime.now().plusDays(RESERVATION_EXPIRE_DAYS));
            this.updateById(firstReservation);

            for (int i = 1; i < waitingList.size(); i++) {
                waitingList.get(i).setStatus("expired");
                this.updateById(waitingList.get(i));
            }
        }
    }

    private ReservationVO convertToVO(Reservation reservation) {
        ReservationVO vo = new ReservationVO();
        BeanUtils.copyProperties(reservation, vo);

        User user = userMapper.selectById(reservation.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }

        Book book = bookMapper.selectById(reservation.getBookId());
        if (book != null) {
            vo.setBookName(book.getBookName());
            vo.setAuthor(book.getAuthor());
            vo.setCoverImg(book.getCoverImg());
        }

        vo.setStatusText(getStatusText(reservation.getStatus()));

        if ("available".equals(reservation.getStatus()) && reservation.getExpireTime() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getExpireTime());
            vo.setDaysRemaining((int) daysRemaining);
        }

        return vo;
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "waiting" -> "等待中";
            case "available" -> "可借阅";
            case "converted" -> "已转借阅";
            case "cancelled" -> "已取消";
            case "expired" -> "已过期";
            default -> status;
        };
    }
}
