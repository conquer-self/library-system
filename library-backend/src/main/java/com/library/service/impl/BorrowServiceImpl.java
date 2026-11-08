package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.dto.BorrowQueryDTO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.BorrowService;
import com.library.service.ReservationService;
import com.library.vo.BorrowRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class BorrowServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationService reservationService;

    //TODO 实现借阅申请业务逻辑


    @Override
    public void applyBorrow(Long userId, Long bookId) {

            // 1. 用户状态校验
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new BusinessException("您的账号已被禁用，无法借阅");
            }

            // 2. 图书存在校验
            Book book = bookMapper.selectById(bookId);
            if (book == null) {
                throw new BusinessException("图书不存在");
            }

            // 3. 图书状态校验（上架为1）

            if (book.getStatus() != 1) {
                throw new BusinessException("图书已下架，无法借阅");
            }

            // 4. 图书库存校验
            if (book.getStock() <= 0) {
                throw new BusinessException("该图书暂无库存");
            }

            // 5. 待审核数量校验（<=3）
            LambdaQueryWrapper<BorrowRecord> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(BorrowRecord::getUserId, userId)
                    .eq(BorrowRecord::getStatus, "pending");
            long pendingCount = this.count(pendingWrapper);
            if (pendingCount >= 3) {
                throw new BusinessException("您有太多待审核的申请，请等待审核后再试");
            }

            // 6. 借阅上限校验（总数<=5）
            LambdaQueryWrapper<BorrowRecord> limitWrapper = new LambdaQueryWrapper<>();
            limitWrapper.eq(BorrowRecord::getUserId, userId)
                    .in(BorrowRecord::getStatus, "pending", "borrowing", "returning");
            long borrowCount = this.count(limitWrapper);
            if (borrowCount >= 5) {
                throw new BusinessException("您已达到最大借阅数量（5本）");
            }

            // 7. 重复借阅校验
            LambdaQueryWrapper<BorrowRecord> repeatWrapper = new LambdaQueryWrapper<>();
            repeatWrapper.eq(BorrowRecord::getUserId, userId)
                    .eq(BorrowRecord::getBookId, bookId)
                    .in(BorrowRecord::getStatus, "pending", "borrowing", "returning");
            if (this.count(repeatWrapper) > 0) {
                throw new BusinessException("您已借阅或正在申请该图书，不能重复借");
            }

            // 8. 创建借阅记录
            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setStatus("pending");
            record.setRenewCount(0);
            this.save(record);
        }

    @Override
    public void applyReturn(Long userId, Long id) {
        // 1. 记录存在校验：借阅记录存在
        BorrowRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // 2. 用户归属校验：记录属于当前登录用户
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此借阅记录");
        }

        // 3. 记录状态校验：status = 'borrowing' 或 'overdue'
        if (!"borrowing".equals(record.getStatus()) && !"overdue".equals(record.getStatus())) {
            throw new BusinessException("当前状态无法申请还书");
        }

        record.setStatus("returning");
        this.updateById(record);
    }

    @Override
    public void renewBorrow(Long userId, Long id) {
        // 1. 记录存在校验：借阅记录存在
        BorrowRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // 2. 用户归属校验：记录属于当前登录用户
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此借阅记录");
        }

        // 3. 记录状态校验：status = 'borrowing'
        if (!"borrowing".equals(record.getStatus())) {
            throw new BusinessException("当前状态无法续借");
        }

        // 4. 续借次数校验：renew_count < 1
        if (record.getRenewCount() >= 1) {
            throw new BusinessException("已达到最大续借次数");
        }

        // 5. 逾期校验：due_time > 当前时间
        if (record.getDueTime() != null && record.getDueTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("已逾期，无法续借");
        }

        // 续借延长30天
        record.setDueTime(record.getDueTime().plusDays(30));
        record.setRenewCount(record.getRenewCount() + 1);

        this.updateById(record);
    }

    @Override
    public IPage<BorrowRecordVO> getMyBorrowList(Long userId, BorrowQueryDTO queryDTO) {
        Page<BorrowRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId);

        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(BorrowRecord::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(BorrowRecord::getCreateTime);

        IPage<BorrowRecord> recordPage = this.page(page, wrapper);

        // 转换为VO并填充图书信息
        return recordPage.convert(this::convertToVO);
    }

    @Override
    public IPage<BorrowRecordVO> getAllBorrowList(BorrowQueryDTO queryDTO) {
        Page<BorrowRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(BorrowRecord::getStatus, queryDTO.getStatus());
        }

        // 关键字搜索（书名或借阅人）- 需要关联查询，这里简化处理
        // 如果需要精确搜索，可以使用XML映射

        wrapper.orderByDesc(BorrowRecord::getCreateTime);

        IPage<BorrowRecord> recordPage = this.page(page, wrapper);

        // 转换为VO并填充用户和图书信息
        return recordPage.convert(this::convertToVO);
    }

    //TODO 编写审核通过业务逻辑
// 审核通过
    @Transactional
    @Override
    public void approveBorrow(Long id) {
        // TODO 1: 记录存在校验
        BorrowRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // TODO 2: 记录状态校验
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("该记录不是待审核状态，无法审核");
        }

        // TODO 3: 图书库存二次校验
        Book book = bookMapper.selectById(record.getBookId());
        if (book == null || book.getStock() <= 0) {
            throw new BusinessException("该图书库存不足，无法通过审核");
        }

        // TODO 4: 借阅上限二次校验
        LambdaQueryWrapper<BorrowRecord> limitWrapper = new LambdaQueryWrapper<>();
        limitWrapper.eq(BorrowRecord::getUserId, record.getUserId())
                .in(BorrowRecord::getStatus, "pending", "borrowing", "returning");
        long borrowCount = this.count(limitWrapper);
        if (borrowCount >= 5) {
            throw new BusinessException("该用户已达借阅上限，无法通过审核");
        }

        // TODO 5: 重复借阅二次校验
        LambdaQueryWrapper<BorrowRecord> repeatWrapper = new LambdaQueryWrapper<>();
        repeatWrapper.eq(BorrowRecord::getUserId, record.getUserId())
                .eq(BorrowRecord::getBookId, record.getBookId())
                .in(BorrowRecord::getStatus, "borrowing", "returning");
        if (this.count(repeatWrapper) > 0) {
            throw new BusinessException("该用户已有该图书的有效借阅，无法重复通过");
        }

        // TODO 6: 更新借阅记录
        LocalDateTime now = LocalDateTime.now();
        record.setStatus("borrowing");
        record.setBorrowTime(now);
        record.setDueTime(now.plusDays(30));
        this.updateById(record);

        // TODO 7: 减少图书库存
        book.setStock(book.getStock() - 1);
        bookMapper.updateById(book);
    }
    // 审核拒绝
    @Transactional
    @Override
    public void rejectBorrow(Long id, String remark) {
        // TODO 1: 记录存在校验
        BorrowRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // TODO 2: 记录状态校验
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("该记录不是待审核状态，无法拒绝");
        }

        // TODO 3: 更新记录
        record.setStatus("rejected");
        record.setRemark(remark); // 设为传入的拒绝原因
        this.updateById(record);
    }
    //TODO 编写审核拒绝业务逻辑

    @Override
    @Transactional
    public void confirmReturn(Long id) {
        // 1. 记录存在校验：借阅记录存在
        BorrowRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }

        // 2. 记录状态校验：status = 'returning'
        if (!"returning".equals(record.getStatus())) {
            throw new BusinessException("该记录不是还书中状态，无法确认");
        }

        // 设置归还时间
        record.setStatus("returned");
        record.setReturnTime(LocalDateTime.now());

        this.updateById(record);

        // 图书库存+1
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setStock(book.getStock() + 1);
            bookMapper.updateById(book);
        }
    }

    /**
     * 转换为VO并填充关联信息
     */
    private BorrowRecordVO convertToVO(BorrowRecord record) {
        BorrowRecordVO vo = new BorrowRecordVO();
        BeanUtils.copyProperties(record, vo);

        // 填充用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }

        // 填充图书信息
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            vo.setBookName(book.getBookName());
            vo.setCoverImg(book.getCoverImg());
            vo.setAuthor(book.getAuthor());
        }

        return vo;
    }
}
