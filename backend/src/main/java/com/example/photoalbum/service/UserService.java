package com.example.photoalbum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.photoalbum.common.Result;
import com.example.photoalbum.dto.LoginDTO;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service // 类直接交给 Spring 管
public class UserService {

    @Autowired
    private UserMapper userMapper;
    // new 一个加密工具
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 直接写方法
    //登录
    public Result<?> register(User user) {
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

    public Result<User> login(LoginDTO loginDTO, HttpSession session) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        // 登录成功，把用户信息存入 Session（非常重要 就是你登录的凭证）
        user.setPassword(null);
        session.setAttribute("loginUser", user);
        return Result.success(user);
    }
}