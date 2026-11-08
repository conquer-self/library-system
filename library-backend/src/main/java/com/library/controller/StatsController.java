package com.library.controller;

import com.library.common.Result;
import com.library.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 系统概览（管理员）
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = statsService.getOverview();
        return Result.success(overview);
    }

    /**
     * 热门图书TOP10（管理员）
     */
    @GetMapping("/hot-books")
    public Result<List<Map<String, Object>>> getHotBooks() {
        List<Map<String, Object>> hotBooks = statsService.getHotBooks();
        return Result.success(hotBooks);
    }
}
