package com.example.photoalbum.common;

import lombok.Data;

@Data //自动写get，set方法
public class Result<T> {//泛型
    private Integer code; // 200成功，400失败，401未登录
    private String msg;//提示信息
    private T data;//真实数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }
    //显示好看的结果 相当于tostring方法

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMsg(msg);
        return result;
    }
    //同理显示错误结果 并详细显示错误信息
}
