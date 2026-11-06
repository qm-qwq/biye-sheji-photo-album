package com.example.photoalbum.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
}
//登录接口用这个而不是User类 专为登录定制的 为了安全