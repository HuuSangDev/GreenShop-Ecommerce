package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.AddToCartRequest;
import com.huusang.demo.Dto.Request.UpdateCartItemRequest;
import com.huusang.demo.Dto.Response.CartResponse;
import com.huusang.demo.Dto.Response.CartValidationResponse;
import com.huusang.demo.Service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {

    CartService cartService;

    // GET /api/v1/cart
    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getCart(getUserId(jwt)))
                .build();
    }

    // POST /api/v1/cart/items
    @PostMapping("/items")
    public ApiResponse<CartResponse> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddToCartRequest request) {
        return ApiResponse.<CartResponse>builder()
                .message("Item added to cart")
                .result(cartService.addToCart(getUserId(jwt), request))
                .build();
    }

    // PUT /api/v1/cart/items/{cartItemId}
    @PutMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> updateQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.<CartResponse>builder()
                .message("Cart item updated")
                .result(cartService.updateQuantity(getUserId(jwt), cartItemId, request))
                .build();
    }

    // DELETE /api/v1/cart/items/{cartItemId}
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String cartItemId) {
        return ApiResponse.<CartResponse>builder()
                .message("Item removed from cart")
                .result(cartService.removeItem(getUserId(jwt), cartItemId))
                .build();
    }

    // DELETE /api/v1/cart
    @DeleteMapping
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        cartService.clearCart(getUserId(jwt));
        return ApiResponse.<Void>builder()
                .message("Cart cleared")
                .build();
    }

    // GET /api/v1/cart/validate
    // Gọi trước khi checkout để kiểm tra giá + tồn kho
    @GetMapping("/validate")
    public ApiResponse<CartValidationResponse> validateCart(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<CartValidationResponse>builder()
                .result(cartService.validateCart(getUserId(jwt)))
                .build();
    }

    // JWT subject là email (xem AuthService.generateToken — dùng user.getEmail() làm subject)
    // CartService.getOrCreateCart nhận email và dùng UserRepository.findByEmail để resolve
    private String getUserId(Jwt jwt) {
        return jwt.getSubject(); // email của user đang đăng nhập
    }
}
