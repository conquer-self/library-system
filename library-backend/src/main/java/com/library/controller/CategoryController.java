package com.library.controller;

import com.library.common.Result;
import com.library.entity.Category;
import com.library.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类列表（公开接口）
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        List<Category> list = categoryService.getCategoryList();
        return Result.success(list);
    }

    /**
     * 添加分类（管理员）
     */
    @PostMapping("/add")
    public Result<Void> addCategory(@RequestBody Category category) {
        categoryService.addCategory(category);
        return Result.success("添加成功", null);
    }

    /**
     * 修改分类（管理员）
     */
    @PutMapping("/update")
    public Result<Void> updateCategory(@RequestBody Category category) {
        categoryService.updateCategory(category);
        return Result.success("修改成功", null);
    }

    /**
     * 删除分类（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }
}
