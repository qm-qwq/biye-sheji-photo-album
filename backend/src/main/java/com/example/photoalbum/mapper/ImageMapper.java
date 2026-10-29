package com.example.photoalbum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.photoalbum.entity.Image;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageMapper extends BaseMapper<Image> {
}