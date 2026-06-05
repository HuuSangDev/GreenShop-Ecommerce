package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.UserCreationRequest;
import com.huusang.demo.Dto.Response.UserResponse;
import com.huusang.demo.Enum.RoleName;
import com.huusang.demo.Service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    UserService userService ;
    @PostMapping("/register")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request)
    {
        return ApiResponse.<UserResponse>builder()
                .message("create user success")
                .result(userService.createUser(request))
                .build();
    }
    @PostMapping("/acceptRole/{userId}")
    @PreAuthorize("hasAuthority('VERIFY_SELLER') or hasAuthority('MANAGE_USER')")
    public ApiResponse<UserResponse> addRoleToUser(
            @PathVariable String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.addRoletoUser(userId, RoleName.SELLER.name()))
                .message("Đã duyệt tài khoản thành Chủ Shop thành công!")
                .build();
    }

    @GetMapping("/getUsers")
    ApiResponse<List<UserResponse>>getUsers()
    {
        return ApiResponse.<List<UserResponse>>builder()
                .message("get all user success")
                .result(userService.getUsers())
                .build();
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt,
            @RequestBody @Valid com.huusang.demo.Dto.Request.UpdateProfileRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("Cập nhật thông tin thành công")
                .result(userService.updateProfile(jwt.getSubject(), request))
                .build();
    }

    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> uploadAvatar(
            @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.<UserResponse>builder()
                .message("Cập nhật ảnh đại diện thành công")
                .result(userService.uploadAvatar(jwt.getSubject(), file))
                .build();
    }

}
