package com.example.photoalbum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.photoalbum.common.Result;
import com.example.photoalbum.entity.Album;
import com.example.photoalbum.entity.Image;
import com.example.photoalbum.entity.User;
import com.example.photoalbum.mapper.AlbumMapper;
import com.example.photoalbum.mapper.ImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageMapper imageMapper;
    @Autowired
    private AlbumMapper albumMapper;

    @Value("${upload.path:D:/biyesheji/uploads/}")
    private String uploadPath;

    // ================= 1. 图片上传接口（极简版） =================
    @PostMapping("/upload")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file,
                                 @RequestParam("albumId") Long albumId,
                                 HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return Result.error("未登录");

        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(user.getId())) {
            return Result.error("无权向此相册上传图片");
        }

        if (file.isEmpty()) return Result.error("上传文件为空");
        if (file.getSize() > 10 * 1024 * 1024) return Result.error("文件大小不能超过 10MB");

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";
        List<String> allowedExt = Arrays.asList("jpg", "jpeg", "png");
        if (!allowedExt.contains(extension)) return Result.error("只允许上传 jpg/png 格式图片");

        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String dirPath = uploadPath + user.getId() + "/" + datePath + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        File destFile = new File(dirPath + newFileName);

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

        // 存入数据库 (宽高咱们暂时不读了，直接存空，省点事)
        Image image = new Image();
        image.setAlbumId(albumId);
        image.setUserId(user.getId());
        image.setOriginalName(originalFilename);
        image.setFilePath(dirPath + newFileName);
        image.setThumbPath(dirPath + thumbName);
        image.setFileSize(file.getSize());
        image.setContentType(file.getContentType());
        image.setCreatedTime(LocalDateTime.now());
        image.setUpdatedTime(LocalDateTime.now());
        imageMapper.insert(image);

        // 更新相册图片数量
        album.setImageCount(album.getImageCount() + 1);
        albumMapper.updateById(album);

        return Result.success(image);
    }

    // ================= 2. 图片列表分页查询接口 =================
    @GetMapping("/list")
    public Result<?> getImages(@RequestParam Long albumId,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "12") Integer size,
                               HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return Result.error("未登录");

        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(user.getId())) {
            return Result.error("无权查看此相册");
        }

        Page<Image> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Image::getAlbumId, albumId)
                .orderByDesc(Image::getCreatedTime);

        imageMapper.selectPage(pageObj, wrapper);
        return Result.success(pageObj);
    }
    // ================= 3. 删除图片接口 =================
    @DeleteMapping("/{id}")
    public Result<?> deleteImage(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return Result.error("未登录");

        Image image = imageMapper.selectById(id);
        if (image == null || !image.getUserId().equals(user.getId())) {
            return Result.error("无权删除此图片");
        }

        // 1. 逻辑删除数据库记录 (因为有 @TableLogic，自动变成 update is_deleted=1)
        imageMapper.deleteById(id);

        // 2. 物理删除硬盘上的文件 (原图和缩略图)
        try {
            if (image.getFilePath() != null) new File(image.getFilePath()).delete();
            if (image.getThumbPath() != null) new File(image.getThumbPath()).delete();
        } catch (Exception e) {
            System.out.println("删除本地文件失败，但不影响数据库记录删除");
        }

        // 3. 相册图片数量 - 1
        Album album = albumMapper.selectById(image.getAlbumId());
        if (album != null && album.getImageCount() > 0) {
            album.setImageCount(album.getImageCount() - 1);
            albumMapper.updateById(album);
        }

        return Result.success(null);
    }
}