package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {
    
    @Autowired
    private BookMapper bookMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;
    
    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 图书总数
        long bookCount = bookMapper.selectCount(null);
        overview.put("bookCount", bookCount);
        
        // 用户总数
        long userCount = userMapper.selectCount(null);
        overview.put("userCount", userCount);
        
        // 借阅总数
        long borrowCount = borrowRecordMapper.selectCount(null);
        overview.put("borrowCount", borrowCount);
        
        // 待审核数量
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getStatus, "pending");
        long pendingCount = borrowRecordMapper.selectCount(wrapper);
        overview.put("pendingCount", pendingCount);
        
        return overview;
    }
    
    @Override
    public List<Map<String, Object>> getHotBooks() {
        // 查询所有借阅记录
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        
        // 按图书ID分组统计借阅次数
        Map<Long, Long> borrowCountMap = allRecords.stream()
                .collect(Collectors.groupingBy(
                        BorrowRecord::getBookId,
                        Collectors.counting()
                ));
        
        // 获取排名前10的图书ID
        List<Map.Entry<Long, Long>> topBooks = borrowCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        // 构建结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : topBooks) {
            Map<String, Object> item = new HashMap<>();
            Long bookId = entry.getKey();
            Long count = entry.getValue();
            
            Book book = bookMapper.selectById(bookId);
            if (book != null) {
                item.put("bookId", bookId);
                item.put("bookName", book.getBookName());
                item.put("borrowCount", count);
                result.add(item);
            }
        }
        
        return result;
    }
}
