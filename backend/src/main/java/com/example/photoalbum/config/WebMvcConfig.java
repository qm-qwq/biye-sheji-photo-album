//注册拦截器
package com.example.photoalbum.config;

import com.example.photoalbum.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:D:/biyesheji/uploads/}")//硬编码解耦器
    private String uploadPath;
    // 注册拦截器
    @Override//防错标签
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**") //拦截
                .excludePathPatterns("/user/register", "/user/login", "/test.html", "/*.html", "/*.css", "/*.js", "/uploads/**");//放行
    }//拦截器 没登录不让进
//
//    // 解决跨域问题（新加的）
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOriginPatterns("*") // 允许所有前端地址访问
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true) // 允许携带 Cookie (Session 需要)
//                .maxAge(3600);
//    }
    @Override
    //静态资源映射
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);//找文件返回前端
    }
}