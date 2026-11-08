package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.Result;
import com.library.dto.BookQueryDTO;
import com.library.entity.Book;
import com.library.service.BookService;
import com.library.vo.BookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 图书列表（公开接口）
     */
    @GetMapping("/list")
    public Result<IPage<BookVO>> getBookList(BookQueryDTO queryDTO) {
        IPage<BookVO> page = bookService.getBookList(queryDTO);
        return Result.success(page);
    }

    /**
     * 图书详情（公开接口）
     */
    @GetMapping("/{id}")
    public Result<BookVO> getBookDetail(@PathVariable Long id) {
        BookVO bookVO = bookService.getBookDetail(id);
        return Result.success(bookVO);
    }

    /**
     * 添加图书（管理员）
     */
    @PostMapping("/add")
    public Result<Void> addBook(@RequestBody Book book) {
        bookService.addBook(book);
        return Result.success("添加成功", null);
    }

    /**
     * 修改图书（管理员）
     */
    @PutMapping("/update")
    public Result<Void> updateBook(@RequestBody Book book) {
        bookService.updateBook(book);
        return Result.success("修改成功", null);
    }

    /**
     * 删除图书（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success("删除成功", null);
    }
}
