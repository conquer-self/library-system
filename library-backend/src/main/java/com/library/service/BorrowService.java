package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.BorrowQueryDTO;
import com.library.entity.BorrowRecord;
import com.library.vo.BorrowRecordVO;

public interface BorrowService extends IService<BorrowRecord> {

    /**
     * //TODO 声明借阅申请的 Service 接口方法
     * 借书申请（学生）
     */
// 审核通过
    void approveBorrow(Long id);
    // 审核拒绝
    void rejectBorrow(Long id, String remark);



    /**
     * 还书申请（学生）
     */
    void applyReturn(Long userId, Long id);

    /**
     * 续借（学生）
     */
    void renewBorrow(Long userId, Long id);

    /**
     * 我的借阅记录（学生）
     */
    IPage<BorrowRecordVO> getMyBorrowList(Long userId, BorrowQueryDTO queryDTO);

    /**
     * 所有借阅记录（管理员）
     */
    IPage<BorrowRecordVO> getAllBorrowList(BorrowQueryDTO queryDTO);


    //编写审核通过service接口


    //编写审核拒绝service接口


    /**
     * 确认还书（管理员）
     */
    void confirmReturn(Long id);

    void applyBorrow(Long userId, Long bookId);
}
