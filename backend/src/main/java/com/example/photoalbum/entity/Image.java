package com.example.photoalbum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("image")//告诉表名
public class Image {
    @TableId(type = IdType.AUTO)//主键 自增
    private Long id;
    private Long albumId;
    private Long userId;
    private String originalName;
    private String filePath;
    private String thumbPath;
    private Long fileSize;
    private String contentType;
    private Integer width;
    private Integer height;
    private LocalDateTime shotTime; // 拍摄时间(EXIF，D10做)
    private String make;
    private String model;
    private Double fnumber;
    private Integer iso;
    private Integer orientation;

    @TableLogic //逻辑删除  记录时间
    private Integer isDeleted;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}