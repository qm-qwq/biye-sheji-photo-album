package com.example.photoalbum.controller;

import com.example.photoalbum.common.Result;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService; // 👈 注入 Service

    // 辅助方法：获取当前登录用户 ID
    private Long getCurrentUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getId() : null;
    }

    // 1. 图片上传
    @PostMapping("/upload")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file,
                                 @RequestParam("albumId") Long albumId,
                                 HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");
        // 只负责把参数传给 Service
        return imageService.uploadImage(file, albumId, userId);
    }

    // 2. 图片列表分页查询
    @GetMapping("/list")
    public Result<?> getImages(@RequestParam Long albumId,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "12") Integer size,
                               HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");
        return imageService.getImages(albumId, page, size, userId);
    }

    // 3. 删除图片
    @DeleteMapping("/{id}")
    public Result<?> deleteImage(@PathVariable Long id, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");
        return imageService.deleteImage(id, userId);
    }
}