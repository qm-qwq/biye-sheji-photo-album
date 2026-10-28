create database if not exists photo_album
default character set utf8mb4
collate utf8mb4_general_ci;

use photo_album;

-- 用户表
create table if not exists `user` (
  `id` bigint unsigned not null auto_increment comment '主键',
  `username` varchar(50) not null comment '登录名',
  `password` varchar(100) not null comment 'BCrypt哈希',
  `nickname` varchar(50) default null comment '昵称',
  `created_time` datetime not null default current_timestamp,
  `updated_time` datetime not null default current_timestamp on update current_timestamp,
  primary key (`id`),
  unique key `uk_user_username` (`username`)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_general_ci comment='用户表';

-- 相册表
create table if not exists `album` (
  `id` bigint unsigned not null auto_increment comment '主键',
  `user_id` bigint unsigned not null comment '所属用户',
  `name` varchar(100) not null comment '相册名',
  `description` varchar(500) default null comment '描述',
  `image_count` int not null default 0 comment '图片数量',
  `is_deleted` tinyint not null default 0 comment '逻辑删除：0否1是',
  `created_time` datetime not null default current_timestamp,
  `updated_time` datetime not null default current_timestamp on update current_timestamp,
  primary key (`id`),
  key `idx_album_user_deleted` (`user_id`, `is_deleted`)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_general_ci comment='相册表';

-- 图片表
create table if not exists `image` (
  `id` bigint unsigned not null auto_increment comment '主键',
  `album_id` bigint unsigned not null comment '所属相册',
  `user_id` bigint unsigned not null comment '所属用户',
  `original_name` varchar(255) not null comment '原文件名',
  `file_path` varchar(500) not null comment '原图路径',
  `thumb_path` varchar(500) default null comment '缩略图路径',
  `file_size` bigint not null default 0 comment '文件大小',
  `content_type` varchar(100) default null comment 'MIME类型',
  `width` int default null comment '原图宽',
  `height` int default null comment '原图高',
  `shot_time` datetime default null comment '拍摄时间，可NULL',
  `make` varchar(100) default null comment '厂商',
  `model` varchar(100) default null comment '型号',
  `fnumber` decimal(4,1) default null comment '光圈',
  `iso` int default null comment 'ISO',
  `orientation` tinyint default null comment 'EXIF方向',
  `is_deleted` tinyint not null default 0 comment '逻辑删除：0否1是',
  `created_time` datetime not null default current_timestamp,
  `updated_time` datetime not null default current_timestamp on update current_timestamp,
  primary key (`id`),
  key `idx_image_album_user_deleted_shot` (`album_id`, `user_id`, `is_deleted`, `shot_time`),
  key `idx_image_user_deleted` (`user_id`, `is_deleted`)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_general_ci comment='图片表';