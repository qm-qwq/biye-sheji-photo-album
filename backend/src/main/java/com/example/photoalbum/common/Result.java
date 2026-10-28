package com.example.photoalbum.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200成功，400失败，401未登录
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMsg(msg);
        return result;
    }
}