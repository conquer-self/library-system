package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.LoginDTO;
import com.library.dto.PasswordDTO;
import com.library.dto.RegisterDTO;
import com.library.entity.User;
import com.library.vo.UserVO;

public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    UserVO login(LoginDTO loginDTO);
    
    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);
    
    /**
     * 获取当前用户信息
     */
    UserVO getUserInfo(Long userId);
    
    /**
     * 修改个人信息
     */
    void updateUserInfo(Long userId, User user);
    
    /**
     * 修改密码
     */
    void updatePassword(Long userId, PasswordDTO passwordDTO);
    
    /**
     * 用户列表（管理员）
     */
    IPage<User> getUserList(Integer pageNum, Integer pageSize, String keyword);
    
    /**
     * 修改用户状态（管理员）
     */
    void updateStatus(Long id, Integer status);
}
