package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.Result;
import com.library.dto.BorrowQueryDTO;
import com.library.service.BorrowService;
import com.library.vo.BorrowRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    //TODO 编写借阅申请接口
    @PostMapping("/apply")
    public Result<Void> applyBorrow(
            @RequestAttribute("userId") Long userId,//从请求属性中
            @RequestBody Map<String, Long> params) {

        Long bookId = params.get("bookId");

        borrowService.applyBorrow(userId, bookId);

        return Result.success("借书申请已提交", null);
    }
    /**
     * 还书申请（学生）
     */
    @PostMapping("/return/{id}")
    public Result<Void> applyReturn(@RequestAttribute("userId") Long userId,
                                    @PathVariable Long id) {
        borrowService.applyReturn(userId, id);
        return Result.success("还书申请已提交", null);
    }


    /**
     * 续借（学生）
     */
    @PostMapping("/renew/{id}")
    public Result<Void> renewBorrow(@RequestAttribute("userId") Long userId,
                                    @PathVariable Long id) {
        borrowService.renewBorrow(userId, id);
        return Result.success("续借成功", null);
    }

    /**
     * 我的借阅记录（学生）
     */
    @GetMapping("/my")
    public Result<IPage<BorrowRecordVO>> getMyBorrowList(@RequestAttribute("userId") Long userId,
                                                          BorrowQueryDTO queryDTO) {
        IPage<BorrowRecordVO> page = borrowService.getMyBorrowList(userId, queryDTO);
        return Result.success(page);
    }

    /**
     * 所有借阅记录（管理员）
     */
    @GetMapping("/list")
    public Result<IPage<BorrowRecordVO>> getAllBorrowList(BorrowQueryDTO queryDTO) {
        IPage<BorrowRecordVO> page = borrowService.getAllBorrowList(queryDTO);
        return Result.success(page);
    }

    //TODO 编写审核通过接口
    @PostMapping("/approve/{id}")
    public Result<String> approveBorrow(@PathVariable Long id) {
        borrowService.approveBorrow(id);
        return Result.success("审核通过");
    }

    @PostMapping("/reject/{id}")
    public Result<String> rejectBorrow(@PathVariable Long id, @RequestParam String remark) {
        borrowService.rejectBorrow(id, remark);
        return Result.success("审核拒绝");
    }

    //TODO 编写审核拒绝接口
    

    /**
     * 确认还书（管理员）
     */
    @PutMapping("/confirm/{id}")
    public Result<Void> confirmReturn(@PathVariable Long id) {
        borrowService.confirmReturn(id);
        return Result.success("还书已确认", null);
    }
}
