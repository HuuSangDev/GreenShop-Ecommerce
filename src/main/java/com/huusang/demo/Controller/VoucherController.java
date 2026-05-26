package com.huusang.demo.Controller;

import com.huusang.demo.Dto.Request.ApplyVoucherRequest;
import com.huusang.demo.Dto.Request.VoucherCreateRequest;
import com.huusang.demo.Dto.Request.VoucherUpdateRequest;
import com.huusang.demo.Dto.Response.ApiResponse;
import com.huusang.demo.Dto.Response.ApplyVoucherResponse;
import com.huusang.demo.Dto.Response.VoucherResponse;
import com.huusang.demo.Service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // ==================== CRUD (Admin + Seller) ====================

    // Seller dùng CREATE_PRODUCT, Admin dùng MANAGE_USER
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody VoucherCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo voucher thành công", voucherService.createVoucher(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @PathVariable String id,
            @Valid @RequestBody VoucherUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật voucher thành công", voucherService.updateVoucher(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ResponseEntity<ApiResponse<VoucherResponse>> deactivateVoucher(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Đã vô hiệu hóa voucher", voucherService.deactivateVoucher(id)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ResponseEntity<ApiResponse<VoucherResponse>> activateVoucher(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Đã kích hoạt lại voucher", voucherService.activateVoucher(id)));
    }

    // ==================== SELLER ====================

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAuthority('VIEW_SHOP_ORDERS')")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getVouchersByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getVouchersByShop(shopId)));
    }

    // ==================== CUSTOMER ====================

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAvailableVouchers(
            @RequestParam BigDecimal orderAmount,
            @RequestParam(required = false) Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getAvailableVouchers(orderAmount, shopId)));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplyVoucherResponse>> applyVoucher(
            @Valid @RequestBody ApplyVoucherRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Áp dụng voucher thành công", voucherService.applyVoucher(request)));
    }
}