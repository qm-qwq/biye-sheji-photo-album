package com.example.photoalbum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("album")
public class Album {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 归属于哪个用户
    private String name; // 相册名
    private String description; // 描述
    private Integer imageCount; // 图片数量

    @TableLogic // 逻辑删除注解
    private Integer isDeleted;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}