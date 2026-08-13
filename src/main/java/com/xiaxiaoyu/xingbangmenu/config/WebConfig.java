package com.xiaxiaoyu.xingbangmenu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.storage-path:./uploads}")
    private String storagePath;

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 注意：storagePath 是相对路径时，会依赖 JVM 启动时的工作目录（user.dir）。
        // 从不同目录启动（如命令行在项目根执行 mvn）会导致 ./uploads 解析到错误位置 → 图片全部 404。
        // 这里强制转为绝对路径并补全尾斜杠（Spring 要求 location 以 / 结尾），启动时打印实际路径便于排查。
        String absolutePath = Paths.get(storagePath).toAbsolutePath().normalize().toUri().toString();
        if (!absolutePath.endsWith("/")) {
            absolutePath = absolutePath + "/";
        }
        System.out.println("[WebConfig] 静态资源 /uploads/** 映射到: " + absolutePath);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/auth/login");
    }
}
