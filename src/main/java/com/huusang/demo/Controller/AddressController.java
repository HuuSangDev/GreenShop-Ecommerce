package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.AddressRequest;
import com.huusang.demo.Dto.Response.AddressResponse;
import com.huusang.demo.Service.AddressService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API quản lý địa chỉ giao hàng của User.
 * <p>
 * Base URL: /api/v1/addresses
 * Tất cả endpoint đều yêu cầu xác thực (isAuthenticated).
 */
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressController {

    AddressService addressService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/addresses — Thêm địa chỉ mới
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Thêm địa chỉ giao hàng mới.
     * <p>
     * Logic đặc biệt:
     * - Địa chỉ ĐẦU TIÊN → tự động isDefault = true
     * - isDefault = true  → reset địa chỉ cũ → set mới làm mặc định
     * - isDefault = false → lưu bình thường
     *
     * Postman: POST /api/v1/addresses
     * Body JSON:
     * {
     *   "fullName": "Nguyễn Văn A",
     *   "phone": "0912345678",
     *   "street": "123 Đường ABC",
     *   "ward": "Phường Vĩnh Phúc",
     *   "district": "Quận Ba Đình",
     *   "province": "Thành phố Hà Nội",
     *   "wardCode": "1A0807",
     *   "districtId": 1482,
     *   "provinceId": 201,
     *   "isDefault": true
     * }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AddressResponse> addAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressRequest request) {

        return ApiResponse.<AddressResponse>builder()
                .code(201)
                .message("Thêm địa chỉ thành công")
                .result(addressService.addAddress(getEmail(jwt), request))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/addresses — Danh sách địa chỉ (mặc định lên đầu)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ địa chỉ giao hàng của user đang đăng nhập.
     * Địa chỉ mặc định luôn ở đầu danh sách.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<AddressResponse>> getMyAddresses(
            @AuthenticationPrincipal Jwt jwt) {

        return ApiResponse.<List<AddressResponse>>builder()
                .message("Danh sách địa chỉ giao hàng")
                .result(addressService.getMyAddresses(getEmail(jwt)))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/addresses/default — Lấy địa chỉ mặc định
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lấy địa chỉ giao hàng mặc định — dùng để auto-fill trang Checkout.
     * Trả về districtId + wardCode để frontend gọi preview tính phí ship ngay.
     * Ném lỗi ADDRESS_NO_DEFAULT nếu user chưa có địa chỉ.
     */
    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AddressResponse> getDefaultAddress(
            @AuthenticationPrincipal Jwt jwt) {

        return ApiResponse.<AddressResponse>builder()
                .message("Địa chỉ giao hàng mặc định")
                .result(addressService.getDefaultAddress(getEmail(jwt)))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/addresses/{id} — Cập nhật địa chỉ
    // ─────────────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody AddressRequest request) {

        return ApiResponse.<AddressResponse>builder()
                .message("Cập nhật địa chỉ thành công")
                .result(addressService.updateAddress(getEmail(jwt), id, request))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/addresses/{id}/set-default — Đặt làm mặc định
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Đặt một địa chỉ làm mặc định — không cần gửi toàn bộ body.
     * Tự động reset địa chỉ cũ về false.
     */
    @PatchMapping("/{id}/set-default")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AddressResponse> setDefault(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        return ApiResponse.<AddressResponse>builder()
                .message("Đã đặt làm địa chỉ mặc định")
                .result(addressService.setDefault(getEmail(jwt), id))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/addresses/{id} — Xóa địa chỉ
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Xóa địa chỉ. Không cho xóa địa chỉ mặc định (phòng user vô tình mất mặc định).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        addressService.deleteAddress(getEmail(jwt), id);
        return ApiResponse.<Void>builder()
                .message("Xóa địa chỉ thành công")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private String getEmail(Jwt jwt) {
        return jwt.getSubject(); // JWT subject là email
    }
}
