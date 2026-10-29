package com.example.photoalbum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.photoalbum.common.Result;
import com.example.photoalbum.entity.Album;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.mapper.AlbumMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    @Autowired
    private AlbumMapper albumMapper;

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

        album.setUserId(userId);
        album.setImageCount(0);
        album.setCreatedTime(LocalDateTime.now());
        album.setUpdatedTime(LocalDateTime.now());
        albumMapper.insert(album);
        return Result.success(album);
    }

    // 2. 查询当前用户的相册列表
    @GetMapping
    public Result<List<Album>> getMyAlbums(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");

        // 查询条件：user_id = 当前用户
        LambdaQueryWrapper<Album> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Album::getUserId, userId).orderByDesc(Album::getCreatedTime);

        List<Album> list = albumMapper.selectList(wrapper);
        return Result.success(list);
    }

    // 3. 修改相册名/描述
    @PutMapping
    public Result<?> updateAlbum(@RequestBody Album album, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");

        // 安全校验：先查出来，看看这个相册是不是当前用户的（这就是 D5 要验收的权限隔离）
        Album oldAlbum = albumMapper.selectById(album.getId());
        if (oldAlbum == null || !oldAlbum.getUserId().equals(userId)) {
            return Result.error("无权修改此相册");
        }

        album.setUserId(userId); // 防止前端偷偷篡改 userId
        album.setUpdatedTime(LocalDateTime.now());
        albumMapper.updateById(album);
        return Result.success(null);
    }

    // 4. 删除相册（逻辑删除）
    @DeleteMapping("/{id}")
    public Result<?> deleteAlbum(@PathVariable Long id, HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) return Result.error("未登录");

        // 安全校验：只能删自己的
        Album oldAlbum = albumMapper.selectById(id);
        if (oldAlbum == null || !oldAlbum.getUserId().equals(userId)) {
            return Result.error("无权删除此相册");
        }

        albumMapper.deleteById(id); // 因为有 @TableLogic 注解，这里只会把 is_deleted 改成 1
        return Result.success(null);
    }
}