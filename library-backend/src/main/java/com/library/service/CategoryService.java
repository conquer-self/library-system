package com.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    
    /**
     * 获取分类列表（按排序号排序）
     */
    List<Category> getCategoryList();
    
    /**
     * 添加分类
     */
    void addCategory(Category category);
    
    /**
     * 修改分类
     */
    void updateCategory(Category category);
    
    /**
     * 删除分类（检查是否有图书）
     */
    void deleteCategory(Long id);
}
