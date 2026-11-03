package com.example.photoalbum.controller;

import com.example.photoalbum.common.Result;
import com.example.photoalbum.dto.LoginDTO;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. 注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    // 2. 登录
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        return userService.login(loginDTO, session);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        // 清空 Session
        session.invalidate();
        return Result.success(null);
    }
}