package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.*;
import com.huusang.demo.Dto.Response.ProductResponse;
import com.huusang.demo.Dto.Response.ProductVariantResponse;
import com.huusang.demo.Service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ProductController {

    ProductService productService;

    // ==================== SELLER ENDPOINTS ====================

    /**
     * POST /products
     * Tạo sản phẩm mới kèm ảnh.
     * Postman: Body → form-data
     *   productName, description, price, stockQuantity, categoryId → Text
     *   image → File (optional)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ProductResponse> createProduct(
            @AuthenticationPrincipal Jwt jwt,
            @ModelAttribute @Valid ProductCreateRequest request) {

        return ApiResponse.<ProductResponse>builder()
                .code(201)
                .message("Tạo sản phẩm thành công")
                .result(productService.createProduct(request, getEmail(jwt)))
                .build();
    }

    /**
     * PUT /products/{id}
     * Cập nhật thông tin sản phẩm (ảnh tùy chọn).
     * Postman: Body → form-data
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ProductResponse> updateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @ModelAttribute @Valid ProductUpdateRequest request) {

        return ApiResponse.<ProductResponse>builder()
                .message("Cập nhật sản phẩm thành công")
                .result(productService.updateProduct(id, request, getEmail(jwt)))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> deleteProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        productService.deleteProduct(id, getEmail(jwt));
        return ApiResponse.<Void>builder()
                .message("Xóa sản phẩm thành công")
                .build();
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
        return ApiResponse.<ProductResponse>builder()
                .message("Thông tin sản phẩm")
                .result(productService.getProductById(id))
                .build();
    }

    @GetMapping("/shop/{shopId}")
    public ApiResponse<Page<ProductResponse>> getProductsByShop(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .message("Danh sách sản phẩm của shop")
                .result(productService.getProductsByShop(shopId, page, size))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .message("Kết quả tìm kiếm")
                .result(productService.searchProducts(keyword, page, size))
                .build();
    }

    @PostMapping("/filter")
    public ApiResponse<Page<ProductResponse>> filterProducts(
            @RequestBody ProductFilterRequest request) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .message("Kết quả lọc sản phẩm")
                .result(productService.filterProducts(request))
                .build();
    }

    @GetMapping("/top-selling")
    public ApiResponse<Page<ProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shopId) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .message("Sản phẩm bán chạy nhất")
                .result(productService.getTopSellingProducts(page, size, shopId))
                .build();
    }

    @GetMapping("/new-arrivals")
    public ApiResponse<Page<ProductResponse>> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .message("Sản phẩm mới nhất")
                .result(productService.getNewArrivals(page, size))
                .build();
    }

    // ==================== VARIANT ENDPOINTS ====================

    @PostMapping("/{productId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ProductVariantResponse> addVariant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request) {

        return ApiResponse.<ProductVariantResponse>builder()
                .code(201)
                .message("Thêm variant thành công")
                .result(productService.addVariant(productId, request, getEmail(jwt)))
                .build();
    }

    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ProductVariantResponse> updateVariant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantUpdateRequest request) {

        return ApiResponse.<ProductVariantResponse>builder()
                .message("Cập nhật variant thành công")
                .result(productService.updateVariant(variantId, request, getEmail(jwt)))
                .build();
    }

    @DeleteMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> deleteVariant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long variantId) {

        productService.deleteVariant(variantId, getEmail(jwt));
        return ApiResponse.<Void>builder()
                .message("Xóa variant thành công")
                .build();
    }

    @PatchMapping("/variants/{variantId}/stock")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ProductVariantResponse> updateStock(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long variantId,
            @RequestParam Integer quantity) {

        return ApiResponse.<ProductVariantResponse>builder()
                .message("Cập nhật tồn kho thành công")
                .result(productService.updateStock(variantId, quantity, getEmail(jwt)))
                .build();
    }

    // ==================== ADMIN ENDPOINTS ====================

    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> adminHideProduct(@PathVariable Long id) {
        productService.adminHideProduct(id);
        return ApiResponse.<Void>builder()
                .message("Đã ẩn sản phẩm vi phạm")
                .build();
    }

    @PatchMapping("/{id}/unhide")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> adminUnhideProduct(@PathVariable Long id) {
        productService.adminUnhideProduct(id);
        return ApiResponse.<Void>builder()
                .message("Đã mở lại sản phẩm")
                .build();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ProductResponse>> getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<ProductResponse>>builder()
                .message("Danh sách tất cả sản phẩm")
                .result(productService.getAllProductsForAdmin(page, size))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private String getEmail(Jwt jwt) {
        return jwt.getSubject(); // JWT subject là email (xem AuthService.generateToken)
    }
}
