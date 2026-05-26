package com.huusang.demo.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Service lưu file ảnh vào thư mục vật lý uploads/ tại gốc project.
 * <p>
 * Cấu trúc thư mục:
 *   {project-root}/
 *   ├── src/
 *   └── uploads/          ← lưu ảnh tại đây (KHÔNG phải src/main/resources/static)
 *       ├── products/
 *       ├── shops/
 *       └── avatars/
 * <p>
 * storeFile() trả về relative path (ví dụ: "products/uuid.jpg") để lưu vào DB.
 * Frontend truy cập ảnh qua: GET /ecommerce/images/{relativePath}
 */
@Service
@Slf4j
public class FileStorageService {

    /**
     * Đường dẫn tuyệt đối đến thư mục uploads/.
     * Đọc từ application.yaml: file.upload-dir = ${user.dir}/uploads
     * → user.dir là thư mục gốc project khi chạy (nơi có file pom.xml / build.gradle)
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Lưu file ảnh vào thư mục uploads/{subDir}/.
     * Đổi tên file bằng UUID để chống trùng lặp.
     *
     * @param file   MultipartFile nhận từ request
     * @param subDir thư mục con phân loại: "products", "shops", "avatars"
     * @return relative path để lưu vào DB, ví dụ: "products/3f2a1b-uuid.jpg"
     *         Frontend dùng: GET /ecommerce/images/products/3f2a1b-uuid.jpg
     */
    public String storeFile(MultipartFile file, String subDir) {
        validateFile(file);

        // Tạo thư mục vật lý nếu chưa tồn tại
        Path targetDir = Paths.get(uploadDir, subDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            log.error("Không thể tạo thư mục: {}", targetDir, e);
            throw new RuntimeException("Không thể khởi tạo thư mục lưu ảnh", e);
        }

        // Đặt tên file = UUID + đuôi mở rộng gốc (.jpg, .png, ...)
        String extension = extractExtension(file.getOriginalFilename());
        String newFilename = UUID.randomUUID() + extension;

        // Ghi file vào đĩa
        Path destination = targetDir.resolve(newFilename);
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Đã lưu ảnh: uploads/{}/{}", subDir, newFilename);
        } catch (IOException e) {
            log.error("Lỗi ghi file: {}", destination, e);
            throw new RuntimeException("Không thể lưu file ảnh", e);
        }

        // Trả về relative path (dùng forward slash để URL hoạt động trên mọi OS)
        return subDir + "/" + newFilename;
    }

    /**
     * Xóa file vật lý theo relative path đã lưu trong DB.
     * Gọi khi cập nhật ảnh mới để dọn file cũ.
     *
     * @param relativePath ví dụ: "products/uuid.jpg"
     */
    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        Path filePath = Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Đã xóa ảnh: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Không thể xóa ảnh: {}", filePath);
        }
    }

    // ─── PRIVATE ─────────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File không được rỗng");
        if (file.getSize() > MAX_SIZE)
            throw new IllegalArgumentException("File vượt quá giới hạn 10MB");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận ảnh JPEG, PNG, GIF, WebP. Loại nhận được: " + file.getContentType());
    }

    /**
     * Lấy phần đuôi mở rộng từ tên file gốc (lowercase).
     * "photo.JPG" → ".jpg"  |  "image" → ".jpg" (fallback)
     */
    private String extractExtension(String originalFilename) {
        if (originalFilename == null) return ".jpg";
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex >= 0 ? originalFilename.substring(dotIndex).toLowerCase() : ".jpg";
    }
}
