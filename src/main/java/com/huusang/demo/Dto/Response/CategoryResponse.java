package com.huusang.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {
    Long id;
    Long shopId;
    String name;
    String slug;
    String description;
    String imageUrl;
    Long parentId;
    Integer level;
    Integer sortOrder;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Nested children — chỉ có khi gọi getCategoryTree() hoặc getCategoryById()
    List<CategoryResponse> children;
}
