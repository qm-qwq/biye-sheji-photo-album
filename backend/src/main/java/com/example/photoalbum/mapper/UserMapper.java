package com.example.photoalbum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.photoalbum.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper //ai说这玩意不用写增删改查了
public interface UserMapper extends BaseMapper<User> {
}