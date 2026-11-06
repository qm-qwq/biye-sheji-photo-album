package com.example.photoalbum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.photoalbum.common.Result;
import com.example.photoalbum.entity.Album;
import com.example.photoalbum.entity.Image;
import com.example.photoalbum.mapper.AlbumMapper;
import com.example.photoalbum.mapper.ImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private ImageMapper imageMapper;
    @Autowired
    private AlbumMapper albumMapper;
    //因为图片涉及俩所以都喊上

    @Value("${upload.path:D:/biyesheji/uploads/}")
    private String uploadPath;

    // ================= 1. 图片上传业务 =================
    public Result<?> uploadImage(MultipartFile file, Long albumId, Long userId) {
        // 权限校验
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            return Result.error("无权向此相册上传图片");
        }

        if (file.isEmpty()) return Result.error("上传文件为空");
        if (file.getSize() > 10 * 1024 * 1024) return Result.error("文件大小不能超过 10MB");

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";
        List<String> allowedExt = Arrays.asList("jpg", "jpeg", "png");
        if (!allowedExt.contains(extension)) return Result.error("只允许上传 jpg/png 格式图片");

        // 生成日期路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String dirPath = uploadPath + userId + "/" + datePath + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();//如果路径不存在自动创建

        // 生成文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;//防止俩用户图片重名覆盖
        File destFile = new File(dirPath + newFileName);

        //生成缩略图:
        String thumbName = "thumb_" + newFileName;
        File thumbFile = new File(dirPath + thumbName);

        try {
            // 保存原图
            file.transferTo(destFile);
            // 生成缩略图
            net.coobird.thumbnailator.Thumbnails.of(destFile)
                    .size(400, 400)
                    .keepAspectRatio(true)
                    .outputQuality(0.7)
                    .toFile(thumbFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件保存失败");
        }

        // 存入数据库
        Image image = new Image();
        image.setAlbumId(albumId);
        image.setUserId(userId);
        image.setOriginalName(originalFilename);
        image.setFilePath(dirPath + newFileName);
        image.setThumbPath(dirPath + thumbName);
        image.setFileSize(file.getSize());
        image.setContentType(file.getContentType());
        image.setCreatedTime(LocalDateTime.now());
        image.setUpdatedTime(LocalDateTime.now());
        imageMapper.insert(image);//存入，Mybatis-Plus的好处

        // 更新相册图片数量
        album.setImageCount(album.getImageCount() + 1);
        albumMapper.updateById(album);

        return Result.success(image);
    }

    // ================= 2. 分页查询业务 =================
    public Result<?> getImages(Long albumId, Integer page, Integer size, Long userId) {
        // 权限校验
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(userId)) {
            return Result.error("无权查看此相册");
        }

        Page<Image> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getAlbumId, albumId)
                .orderByDesc(Image::getCreatedTime);

        imageMapper.selectPage(pageObj, wrapper);
        return Result.success(pageObj);
    }

    // ================= 3. 删除图片业务 =================
    public Result<?> deleteImage(Long id, Long userId) {
        Image image = imageMapper.selectById(id);
        if (image == null || !image.getUserId().equals(userId)) {
            return Result.error("无权删除此图片");
        }

        // 逻辑删除 DB 记录
        imageMapper.deleteById(id);

        // 物理删除硬盘文件
        try {
            if (image.getFilePath() != null) new File(image.getFilePath()).delete();
            if (image.getThumbPath() != null) new File(image.getThumbPath()).delete();
        } catch (Exception e) {
            System.out.println("删除本地文件失败，但不影响数据库记录删除");//删了数据库顺便把实体文件也删除
        }

        // 相册图片数量 - 1
        Album album = albumMapper.selectById(image.getAlbumId());
        if (album != null && album.getImageCount() > 0) {
            album.setImageCount(album.getImageCount() - 1);
            albumMapper.updateById(album);
        }

        return Result.success(null);
    }
}