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

    @Autowired //依赖注入 要一个mapper干活
    private AlbumMapper albumMapper;

    // 1. 新增相册
    public Result<?> createAlbum(Album album, Long userId) {
        album.setUserId(userId);//强行覆盖前端的userId 防止越权
        album.setImageCount(0);//新建相册图片数量默认0
        album.setCreatedTime(LocalDateTime.now());//记录时间
        album.setUpdatedTime(LocalDateTime.now());
        albumMapper.insert(album);//让mapper放数据库 返回成功信号给前端
        return Result.success(album);
    }

    // 2. 查询我的相册列表
    public Result<List<Album>> getMyAlbums(Long userId) {
        LambdaQueryWrapper<Album> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Album::getUserId, userId).orderByDesc(Album::getCreatedTime);//规矩 userid必须等于用户 由新到旧排序
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