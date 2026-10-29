package com.example.photoalbum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
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

    // 从 application.yml 读取配置的上传路径（咱们暂时写死在本地测试，配置里也可以加）
    @Value("${upload.path:D:/biyesheji/uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file,
                                 @RequestParam("albumId") Long albumId,
                                 HttpSession session) {
        // 1. 登录校验
        User user = (User) session.getAttribute("loginUser");
        if (user == null) return Result.error("未登录");

        // 2. 权限校验：这个相册是不是当前用户的？
        Album album = albumMapper.selectById(albumId);
        if (album == null || !album.getUserId().equals(user.getId())) {
            return Result.error("无权向此相册上传图片");
        }

        // 3. 空文件校验
        if (file.isEmpty()) return Result.error("上传文件为空");

        // 4. 大小校验 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过 10MB");
        }

        // 5. 后缀白名单校验
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";
        List<String> allowedExt = Arrays.asList("jpg", "jpeg", "png");
        if (!allowedExt.contains(extension)) {
            return Result.error("只允许上传 jpg/png 格式图片");
        }

        // 6. 核心安全：真实图片校验 & 像素上限 (50MP)
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                return Result.error("无效的图片文件（伪图片被拦截）");
            }
            long pixels = (long) bufferedImage.getWidth() * bufferedImage.getHeight();
            if (pixels > 50000000L) {
                return Result.error("图片像素过大，不能超过 5000 万像素");
            }
        } catch (IOException e) {
            return Result.error("读取图片失败");
        }

        // 7. 按 用户ID/年/月 生成落盘目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String dirPath = uploadPath + user.getId() + "/" + datePath + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs(); // 如果目录不存在，自动创建
        }

        // 8. UUID 生成新文件名，防止重名覆盖
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        File destFile = new File(dirPath + newFileName);

        try {
            // 保存文件到本地硬盘
            file.transferTo(destFile);
        } catch (IOException e) {
            return Result.error("文件保存失败");
        }

        // 9. 存入数据库
        Image image = new Image();
        image.setAlbumId(albumId);
        image.setUserId(user.getId());
        image.setOriginalName(originalFilename);
        image.setFilePath(dirPath + newFileName);
        image.setFileSize(file.getSize());
        image.setContentType(file.getContentType());
        image.setWidth(bufferedImage.getWidth());
        image.setHeight(bufferedImage.getHeight());
        image.setCreatedTime(LocalDateTime.now());
        image.setUpdatedTime(LocalDateTime.now());

        imageMapper.insert(image);

        // 10. 相册图片数量 +1
        album.setImageCount(album.getImageCount() + 1);
        albumMapper.updateById(album);

        return Result.success(image);
    }
}