package com.example.photoalbum.controller;

import com.example.photoalbum.common.Result;
import com.example.photoalbum.dto.LoginDTO;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController //明确控制层的身份
@RequestMapping("/user") //只有user的才归他管
public class UserController {

    @Autowired //把核心让业务逻辑层去处理
    private UserService userService;

    // 1. 注册
    @PostMapping("/register") //处理注册的接口 post安全
    public Result<?> register(@RequestBody User user) {
        return userService.register(user);
    }//把json转化为java能理解的
//<?>泛型规范的
    // 2. 登录
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginDTO loginDTO, HttpSession session) { //DOT传输数据 http记住谁登录的 session通行证
        return userService.login(loginDTO, session);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        // 清空 Session通行证
        session.invalidate();
        return Result.success(null);
    }
}