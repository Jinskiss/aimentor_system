package com.jins.aimentor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 * 配置静态资源映射，支持上传文件的访问
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用项目根目录 + uploads 作为上传文件目录
        String projectDir = System.getProperty("user.dir");
        String uploadDir = projectDir + File.separator + uploadPath;

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + File.separator);

        System.out.println("[WebMvcConfig] 静态资源映射已配置，上传目录: " + uploadDir);
    }
}