package com.example.photoalbum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.photoalbum.entity.Album;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlbumMapper extends BaseMapper<Album> {
}