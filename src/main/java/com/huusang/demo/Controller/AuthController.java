package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.AuthRequest;
import com.huusang.demo.Dto.Request.LogoutRequest;
import com.huusang.demo.Dto.Request.RefreshRequest;
import com.huusang.demo.Dto.Response.AuthResponse;
import com.huusang.demo.Dto.Response.UserResponse;
import com.huusang.demo.Service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    ApiResponse<AuthResponse> authenticate(@RequestBody @Valid AuthRequest request)
    {
        return ApiResponse.<AuthResponse>builder()
                .message("create user success")
                .result(authService.authenticate(request))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@RequestBody RefreshRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.refreshToken(request))
                .message("Đã cấp lại Access Token mới thành công!")
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        return ApiResponse.<UserResponse>builder()
                .message("Thông tin người dùng hiện tại")
                .result(authService.getMe(email))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @RequestBody LogoutRequest request,
            // Thêm cái này để lấy chuỗi Token từ Header
            @RequestHeader("Authorization") String authorizationHeader) {

        // authorizationHeader có dạng: "Bearer eyJhbGci..."
        // Cắt bỏ 7 ký tự đầu ("Bearer ") để lấy đúng cái chuỗi Token thôi
        String accessToken = authorizationHeader.substring(7);

        // Truyền cả 2 xuống Service
        authService.logout(request, accessToken);

        return ApiResponse.<String>builder()
                .message("Đăng xuất thành công!")
                .build();
    }


}
