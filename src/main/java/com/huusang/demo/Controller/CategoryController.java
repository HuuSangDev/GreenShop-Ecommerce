package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.CategoryRequest;
import com.huusang.demo.Dto.Response.CategoryResponse;
import com.huusang.demo.Service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    // GET /api/v1/categories/tree
    // Hỗ trợ xem tree của shop bất kỳ (public) hoặc xem tree của shop mình (SELLER đã đăng nhập)
    @GetMapping("/tree")
    public ApiResponse<List<CategoryResponse>> getCategoryTree(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long shopId) {
        String email = jwt != null ? jwt.getSubject() : null;
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getCategoryTree(email, shopId))
                .build();
    }

    // GET /api/v1/categories/{id}
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(id))
                .build();
    }

    // POST /api/v1/categories
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<CategoryResponse> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .code(201)
                .message("Category created successfully")
                .result(categoryService.createCategory(request, jwt.getSubject()))
                .build();
    }

    // PUT /api/v1/categories/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<CategoryResponse> updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .message("Category updated successfully")
                .result(categoryService.updateCategory(id, request, jwt.getSubject()))
                .build();
    }

    // DELETE /api/v1/categories/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<Void> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        categoryService.deleteCategory(id, jwt.getSubject());
        return ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }
}
