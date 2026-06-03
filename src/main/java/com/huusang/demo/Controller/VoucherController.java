package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.ApplyVoucherRequest;
import com.huusang.demo.Dto.Request.VoucherCreateRequest;
import com.huusang.demo.Dto.Request.VoucherUpdateRequest;
import com.huusang.demo.Dto.Response.ApplyVoucherResponse;
import com.huusang.demo.Dto.Response.VoucherResponse;
import com.huusang.demo.Service.VoucherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherController {

    VoucherService voucherService;

    // ==================== CRUD (Admin + Seller) ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CREATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ApiResponse<VoucherResponse> createVoucher(
            @Valid @RequestBody VoucherCreateRequest request) {

        return ApiResponse.<VoucherResponse>builder()
                .code(201)
                .message("Tạo voucher thành công")
                .result(voucherService.createVoucher(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ApiResponse<VoucherResponse> updateVoucher(
            @PathVariable String id,
            @Valid @RequestBody VoucherUpdateRequest request) {

        return ApiResponse.<VoucherResponse>builder()
                .message("Cập nhật voucher thành công")
                .result(voucherService.updateVoucher(id, request))
                .build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ApiResponse<VoucherResponse> deactivateVoucher(@PathVariable String id) {
        return ApiResponse.<VoucherResponse>builder()
                .message("Đã vô hiệu hóa voucher")
                .result(voucherService.deactivateVoucher(id))
                .build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or hasAuthority('MANAGE_USER')")
    public ApiResponse<VoucherResponse> activateVoucher(@PathVariable String id) {
        return ApiResponse.<VoucherResponse>builder()
                .message("Đã kích hoạt lại voucher")
                .result(voucherService.activateVoucher(id))
                .build();
    }

    // ==================== SELLER ====================

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAuthority('VIEW_SHOP_ORDERS')")
    public ApiResponse<List<VoucherResponse>> getVouchersByShop(@PathVariable Long shopId) {
        return ApiResponse.<List<VoucherResponse>>builder()
                .message("Danh sách voucher của shop")
                .result(voucherService.getVouchersByShop(shopId))
                .build();
    }

    // ==================== CUSTOMER ====================

    @GetMapping("/available")
    public ApiResponse<List<VoucherResponse>> getAvailableVouchers(
            @RequestParam BigDecimal orderAmount,
            @RequestParam(required = false) Long shopId) {

        return ApiResponse.<List<VoucherResponse>>builder()
                .message("Danh sách voucher khả dụng")
                .result(voucherService.getAvailableVouchers(orderAmount, shopId))
                .build();
    }

    // ==================== ADMIN ====================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<VoucherResponse>> getAllVouchersForAdmin() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .message("Danh sách tất cả voucher")
                .result(voucherService.getAllVouchers())
                .build();
    }

    @PostMapping("/apply")
    public ApiResponse<ApplyVoucherResponse> applyVoucher(
            @Valid @RequestBody ApplyVoucherRequest request) {

        return ApiResponse.<ApplyVoucherResponse>builder()
                .message("Áp dụng voucher thành công")
                .result(voucherService.applyVoucher(request))
                .build();
    }
}