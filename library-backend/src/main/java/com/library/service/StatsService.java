package com.library.service;

import java.util.List;
import java.util.Map;

public interface StatsService {
    
    /**
     * 系统概览数据
     */
    Map<String, Object> getOverview();
    
    /**
     * 热门图书TOP10
     */
    List<Map<String, Object>> getHotBooks();
}
