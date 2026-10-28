package com.example.photoalbum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.photoalbum.common.Result;
import com.example.photoalbum.dto.LoginDTO;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 这里可以直接 new 一个加密工具（不需要放进 Spring 容器）
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 1. 注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        // 密码加密：明文密码进去，出来的是类似 $2a$10$xxxx 的哈希串
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setNickname(user.getNickname() == null ? "新用户" : user.getNickname());
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        userMapper.insert(user);
        return Result.success(null);
    }

    // 2. 登录
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        // 查用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 校验用户和密码
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 登录成功，把用户信息存入 Session（非常重要，这就是你登录的凭证）
        user.setPassword(null); // 为了安全，别把密码传给前端
        session.setAttribute("loginUser", user);

        return Result.success(user);
    }

    // 3. 退出登录
    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        // 清空 Session
        session.invalidate();
        return Result.success(null);
    }
}