package com.example.photoalbum.controller;

import com.example.photoalbum.common.Result;
import com.example.photoalbum.entity.Album;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService; // 注入逻辑层

    // 辅助方法：从 Session 获取当前登录用户 ID
    private Long getCurrentUserId(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? user.getId() : null;
    }

    // 1. 新增相册
    @PostMapping
    public Result<?> createAlbum(@RequestBody Album album, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");
        return albumService.createAlbum(album, userId);
    }

    // 2. 查询当前用户的相册列表
    @GetMapping
    public Result<List<Album>> getMyAlbums(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");
        return albumService.getMyAlbums(userId);
    }

    // 3. 修改相册
    @PutMapping
    public Result<?> updateAlbum(@RequestBody Album album, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");
        return albumService.updateAlbum(album, userId);
    }

    // 4. 删除相册
    @DeleteMapping("/{id}")
    public Result<?> deleteAlbum(@PathVariable Long id, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");
        return albumService.deleteAlbum(id, userId);
    }
}