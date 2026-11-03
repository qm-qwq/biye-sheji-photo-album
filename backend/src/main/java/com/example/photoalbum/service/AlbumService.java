package com.example.photoalbum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.photoalbum.common.Result;
import com.example.photoalbum.entity.Album;
import com.example.photoalbum.mapper.AlbumMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    // 1. 新增相册
    public Result<?> createAlbum(Album album, Long userId) {
        album.setUserId(userId);
        album.setImageCount(0);
        album.setCreatedTime(LocalDateTime.now());
        album.setUpdatedTime(LocalDateTime.now());
        albumMapper.insert(album);
        return Result.success(album);
    }

    // 2. 查询我的相册列表
    public Result<List<Album>> getMyAlbums(Long userId) {
        LambdaQueryWrapper<Album> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Album::getUserId, userId).orderByDesc(Album::getCreatedTime);
        List<Album> list = albumMapper.selectList(wrapper);
        return Result.success(list);
    }

    // 3. 修改相册
    public Result<?> updateAlbum(Album album, Long userId) {
        Album oldAlbum = albumMapper.selectById(album.getId());
        if (oldAlbum == null || !oldAlbum.getUserId().equals(userId)) {
            return Result.error("无权修改此相册");
        }
        album.setUserId(userId);
        album.setUpdatedTime(LocalDateTime.now());
        albumMapper.updateById(album);
        return Result.success(null);
    }

    // 4. 删除相册
    public Result<?> deleteAlbum(Long id, Long userId) {
        Album oldAlbum = albumMapper.selectById(id);
        if (oldAlbum == null || !oldAlbum.getUserId().equals(userId)) {
            return Result.error("无权删除此相册");
        }
        albumMapper.deleteById(id);
        return Result.success(null);
    }
}