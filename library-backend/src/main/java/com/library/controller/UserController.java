package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.Result;
import com.library.dto.LoginDTO;
import com.library.dto.PasswordDTO;
import com.library.dto.RegisterDTO;
import com.library.entity.User;
import com.library.service.UserService;
import com.library.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO);
        return Result.success(userVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功", null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestAttribute("userId") Long userId) {
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestAttribute("userId") Long userId, 
                                       @RequestBody User user) {
        userService.updateUserInfo(userId, user);
        return Result.success("修改成功", null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestAttribute("userId") Long userId,
                                       @RequestBody PasswordDTO passwordDTO) {
        userService.updatePassword(userId, passwordDTO);
        return Result.success("密码修改成功", null);
    }

    /**
     * 用户列表（管理员）
     */
    @GetMapping("/list")
    public Result<IPage<User>> getUserList(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String keyword) {
        IPage<User> page = userService.getUserList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    /**
     * 修改用户状态（管理员）
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, 
                                     @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success("状态修改成功", null);
    }
}
