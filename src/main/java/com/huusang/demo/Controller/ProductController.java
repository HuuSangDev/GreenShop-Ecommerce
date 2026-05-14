package com.huusang.demo.Controller;

import com.huusang.demo.DTO.Request.*;
import com.huusang.demo.DTO.Response.ApiResponse;
import com.huusang.demo.DTO.Response.ProductResponse;
import com.huusang.demo.DTO.Response.ProductVariantResponse;
import com.huusang.demo.Service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ==================== SELLER ====================

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo sản phẩm thành công", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }

    // ==================== PUBLIC ====================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByShop(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByShop(shopId, page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(keyword, page, size)));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> filterProducts(
            @RequestBody ProductFilterRequest request) {

        return ResponseEntity.ok(ApiResponse.success(productService.filterProducts(request)));
    }

    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getTopSellingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shopId) {

        return ResponseEntity.ok(ApiResponse.success(productService.getTopSellingProducts(page, size, shopId)));
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(productService.getNewArrivals(page, size)));
    }

    // ==================== VARIANT ====================

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm variant thành công", productService.addVariant(productId, request)));
    }

    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Cập nhật variant thành công", productService.updateVariant(variantId, request)));
    }

    @DeleteMapping("/variants/{variantId}")
    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long variantId) {
        productService.deleteVariant(variantId);
        return ResponseEntity.ok(ApiResponse.success("Xóa variant thành công", null));
    }

    @PatchMapping("/variants/{variantId}/stock")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateStock(
            @PathVariable Long variantId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(ApiResponse.success("Cập nhật tồn kho thành công", productService.updateStock(variantId, quantity)));
    }

    // ==================== ADMIN ====================

    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasAuthority('MODERATE_PRODUCT')")
    public ResponseEntity<ApiResponse<Void>> adminHideProduct(@PathVariable Long id) {
        productService.adminHideProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Đã ẩn sản phẩm vi phạm", null));
    }

    @PatchMapping("/{id}/unhide")
    @PreAuthorize("hasAuthority('MODERATE_PRODUCT')")
    public ResponseEntity<ApiResponse<Void>> adminUnhideProduct(@PathVariable Long id) {
        productService.adminUnhideProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Đã mở lại sản phẩm", null));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('MODERATE_PRODUCT')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(productService.getAllProductsForAdmin(page, size)));
    }

    @GetMapping("/admin/pending-review")
    @PreAuthorize("hasAuthority('MODERATE_PRODUCT')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsPendingReview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(productService.getProductsPendingReview(page, size)));
    }
}
