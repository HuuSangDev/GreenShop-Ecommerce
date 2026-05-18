package com.huusang.demo.Controller;

import com.huusang.demo.Dto.Request.*;
import com.huusang.demo.Dto.Response.ApiResponse;
import com.huusang.demo.Dto.Response.ProductResponse;
import com.huusang.demo.Dto.Response.ProductVariantResponse;
import com.huusang.demo.Service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ==================== SELLER ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication authentication) {

        ProductResponse response = productService.createProduct(request, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo sản phẩm thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            Authentication authentication) {

        ProductResponse response = productService.updateProduct(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {

        productService.deleteProduct(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByShop(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductResponse> response = productService.getProductsByShop(shopId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductResponse> response = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> filterProducts(
            @RequestBody ProductFilterRequest request) {

        Page<ProductResponse> response = productService.filterProducts(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getTopSellingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shopId) {

        Page<ProductResponse> response = productService.getTopSellingProducts(page, size, shopId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductResponse> response = productService.getNewArrivals(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== VARIANT ENDPOINTS ====================

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request,
            Authentication authentication) {

        ProductVariantResponse response = productService.addVariant(productId, request, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm variant thành công", response));
    }

    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantUpdateRequest request,
            Authentication authentication) {

        ProductVariantResponse response = productService.updateVariant(variantId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật variant thành công", response));
    }

    @DeleteMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable Long variantId,
            Authentication authentication) {

        productService.deleteVariant(variantId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Xóa variant thành công", null));
    }

    @PatchMapping("/variants/{variantId}/stock")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateStock(
            @PathVariable Long variantId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        ProductVariantResponse response = productService.updateStock(variantId, quantity, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tồn kho thành công", response));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminHideProduct(@PathVariable Long id) {
        productService.adminHideProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Đã ẩn sản phẩm vi phạm", null));
    }

    @PatchMapping("/{id}/unhide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminUnhideProduct(@PathVariable Long id) {
        productService.adminUnhideProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Đã mở lại sản phẩm", null));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductResponse> response = productService.getAllProductsForAdmin(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
