package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.exception.BusinessException;
import com.library.mapper.BookMapper;
import com.library.mapper.CategoryMapper;
import com.library.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Autowired
    private BookMapper bookMapper;
    
    @Override
    public List<Category> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return this.list(wrapper);
    }
    
    @Override
    public void addCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }
        
        // 检查分类名称是否已存在
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, category.getName());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }
        
        this.save(category);
    }
    
    @Override
    public void updateCategory(Category category) {
        if (category.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        
        Category existCategory = this.getById(category.getId());
        if (existCategory == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查分类名称是否与其他分类重复
        if (category.getName() != null && !category.getName().trim().isEmpty()) {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getName, category.getName());
            wrapper.ne(Category::getId, category.getId());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("分类名称已存在");
            }
        }
        
        this.updateById(category);
    }
    
    @Override
    public void deleteCategory(Long id) {
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查该分类下是否有图书
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getCategoryId, id);
        long count = bookMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该分类下有图书，无法删除");
        }
        
        this.removeById(id);
    }
}
