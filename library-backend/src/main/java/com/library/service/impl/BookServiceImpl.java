package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.dto.BookQueryDTO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Category;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.CategoryMapper;
import com.library.service.BookService;
import com.library.vo.BookVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;
    
    @Override
    public IPage<BookVO> getBookList(BookQueryDTO queryDTO) {
        Page<Book> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索：书名或作者
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Book::getBookName, queryDTO.getKeyword())
                             .or()
                             .like(Book::getAuthor, queryDTO.getKeyword()));
        }
        
        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Book::getCategoryId, queryDTO.getCategoryId());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Book::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByDesc(Book::getCreateTime);
        
        IPage<Book> bookPage = this.page(page, wrapper);
        
        // 转换为 VO 并填充分类名称
        IPage<BookVO> voPage = bookPage.convert(book -> {
            BookVO vo = new BookVO();
            BeanUtils.copyProperties(book, vo);
            
            // 获取分类名称
            if (book.getCategoryId() != null) {
                Category category = categoryMapper.selectById(book.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
            
            return vo;
        });
        
        return voPage;
    }
    
    @Override
    public BookVO getBookDetail(Long id) {
        Book book = this.getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        
        BookVO vo = new BookVO();
        BeanUtils.copyProperties(book, vo);
        
        // 获取分类名称
        if (book.getCategoryId() != null) {
            Category category = categoryMapper.selectById(book.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        return vo;
    }
    
    @Override
    public void addBook(Book book) {
        // 参数校验
        if (!StringUtils.hasText(book.getBookName())) {
            throw new BusinessException("图书名称不能为空");
        }
        
        // 设置默认值
        if (book.getStock() == null) {
            book.setStock(0);
        }
        if (book.getTotal() == null) {
            book.setTotal(book.getStock());
        }
        if (book.getStatus() == null) {
            book.setStatus(1);
        }
        
        this.save(book);
    }
    
    @Override
    public void updateBook(Book book) {
        if (book.getId() == null) {
            throw new BusinessException("图书ID不能为空");
        }
        
        Book existBook = this.getById(book.getId());
        if (existBook == null) {
            throw new BusinessException("图书不存在");
        }
        
        // 计算借出数量：状态为 pending/borrowing/returning 的借阅记录数
        LambdaQueryWrapper<BorrowRecord> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(BorrowRecord::getBookId, book.getId())
                    .in(BorrowRecord::getStatus, "pending", "borrowing", "returning");
        long borrowedCount = borrowRecordMapper.selectCount(borrowWrapper);
        
        // 校验总数量
        if (book.getTotal() != null) {
            // total >= 0：总数量不能为负数
            if (book.getTotal() < 0) {
                throw new BusinessException("总数量不能为负数");
            }
            
            // total >= borrowedCount：总数量不能小于借出数量
            if (book.getTotal() < borrowedCount) {
                throw new BusinessException("总数量不能小于借出数量（当前有" + borrowedCount + "本借出）");
            }
            
            // 自动计算库存：stock = total - borrowedCount
            book.setStock((int) (book.getTotal() - borrowedCount));
        }
        
        // 校验下架操作
        if (book.getStatus() != null && book.getStatus() == 0 && existBook.getStatus() == 1) {
            // 状态从上架(1)变为下架(0)
            
            // 待审核申请检查：检查是否有 pending 状态的借阅
            LambdaQueryWrapper<BorrowRecord> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(BorrowRecord::getBookId, book.getId())
                         .eq(BorrowRecord::getStatus, "pending");
            long pendingCount = borrowRecordMapper.selectCount(pendingWrapper);
            if (pendingCount > 0) {
                throw new BusinessException("该图书有待审核的借阅申请，请先处理");
            }
        }
        
        this.updateById(book);
    }
    
    @Override
    public void deleteBook(Long id) {
        Book book = this.getById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        
        // 1. 检查图书状态：必须先下架才能删除
        if (book.getStatus() == 1) {
            throw new BusinessException("该图书处于上架状态，请先下架再删除");
        }
        
        // 2. 进行中借阅检查：无 pending/borrowing/returning 状态的记录
        LambdaQueryWrapper<BorrowRecord> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(BorrowRecord::getBookId, id)
                    .in(BorrowRecord::getStatus, "pending", "borrowing", "returning");
        long activeBorrowCount = borrowRecordMapper.selectCount(borrowWrapper);
        if (activeBorrowCount > 0) {
            throw new BusinessException("该图书有未完成的借阅，无法删除");
        }
        
        this.removeById(id);
    }
}
