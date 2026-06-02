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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    // GET /api/v1/categories/tree
    // Public — ai cũng xem được cây danh mục
    @GetMapping("/tree")
    public ApiResponse<List<CategoryResponse>> getCategoryTree() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getCategoryTree())
                .build();
    }

    // GET /api/v1/categories/test - Debug endpoint
    @GetMapping("/test")  
    public ApiResponse<String> testEndpoint() {
        return ApiResponse.<String>builder()
                .result("Categories endpoint is working!")
                .message("Test successful")
                .build();
    }

    // GET /api/v1/categories/count - Count categories
    @GetMapping("/count")
    public ApiResponse<Long> countCategories() {
        long count = categoryService.countAllCategories();
        return ApiResponse.<Long>builder()
                .result(count)
                .message("Total categories: " + count)
                .build();
    }

    // GET /api/v1/categories/all - Get all without tree structure
    @GetMapping("/all")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategoriesFlat())
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
    // Chỉ ADMIN và SELLER mới tạo được
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .code(201)
                .message("Category created successfully")
                .result(categoryService.createCategory(request))
                .build();
    }

    // PUT /api/v1/categories/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .message("Category updated successfully")
                .result(categoryService.updateCategory(id, request))
                .build();
    }

    // DELETE /api/v1/categories/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }
}
