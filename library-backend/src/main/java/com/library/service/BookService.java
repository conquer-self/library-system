package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.BookQueryDTO;
import com.library.entity.Book;
import com.library.vo.BookVO;

public interface BookService extends IService<Book> {
    
    /**
     * 图书列表（分页、搜索、筛选）
     */
    IPage<BookVO> getBookList(BookQueryDTO queryDTO);
    
    /**
     * 图书详情
     */
    BookVO getBookDetail(Long id);
    
    /**
     * 添加图书
     */
    void addBook(Book book);
    
    /**
     * 修改图书
     */
    void updateBook(Book book);
    
    /**
     * 删除图书（检查借阅记录）
     */
    void deleteBook(Long id);
}
