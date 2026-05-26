package com.huusang.demo.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình Spring MVC để serve ảnh tĩnh từ thư mục uploads/ vật lý trên đĩa.
 * <p>
 * URL pattern : GET /ecommerce/images/{subDir}/{filename}
 * File vật lý : {project-root}/uploads/{subDir}/{filename}
 * <p>
 * Ví dụ:
 *   URL  → http://localhost:8080/ecommerce/images/products/uuid.jpg
 *   File → D:/project/E-Commerce/uploads/products/uuid.jpg
 * <p>
 * Tại sao dùng /images/** thay vì serve trực tiếp từ resources/static?
 * → File trong resources/static bị mất mỗi lần build lại (mvn package sẽ xóa).
 * → Thư mục uploads/ nằm ngoài src nên BUILD lại không ảnh hưởng.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối của thư mục uploads/ trên ổ cứng
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // "file:" prefix bắt buộc để Spring biết đây là đường dẫn file hệ thống
        String resourceLocation = "file:" + uploadPath.toString() + "/";

        registry
            .addResourceHandler("/images/**")      // URL pattern frontend gọi
            .addResourceLocations(resourceLocation) // Thư mục vật lý tương ứng
            .setCachePeriod(3600);                  // Cache browser 1 giờ

        // Log để dễ debug khi khởi động
        System.out.println("[FileStorage] Serving /images/** from: " + resourceLocation);
    }
}
