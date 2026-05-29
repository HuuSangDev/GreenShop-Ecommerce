package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.*;
import com.huusang.demo.Dto.Response.ProductRatingSummaryResponse;
import com.huusang.demo.Dto.Response.ReviewResponse;
import com.huusang.demo.Service.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    // POST /api/v1/reviews
    // Chỉ BUYER mới được tạo review
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ApiResponse<ReviewResponse> createReview(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .code(201)
                .message("Review created successfully")
                .result(reviewService.createReview(jwt.getSubject(), request))
                .build();
    }

    // PUT /api/v1/reviews/{reviewId}
    // Chỉ owner review mới được sửa
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('BUYER')")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .message("Review updated successfully")
                .result(reviewService.updateReview(jwt.getSubject(), reviewId, request))
                .build();
    }

    // DELETE /api/v1/reviews/{reviewId}
    // Chỉ ADMIN mới được xóa (soft delete)
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reviewId,
            @Valid @RequestBody DeleteReviewRequest request) {
        reviewService.deleteReview(jwt.getSubject(), reviewId, request);
        return ApiResponse.<Void>builder()
                .message("Review deleted successfully")
                .build();
    }

    // GET /api/v1/reviews/products/{productId}
    // Public — ai cũng xem được
    // Query params: page, size, sortBy (newest | highest_rating | lowest_rating)
    @GetMapping("/products/{productId}")
    public ApiResponse<Page<ReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0")      int    page,
            @RequestParam(defaultValue = "10")     int    size,
            @RequestParam(defaultValue = "newest") String sortBy) {
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getReviewsByProduct(productId, page, size, sortBy))
                .build();
    }

    // GET /api/v1/reviews/products/{productId}/summary
    // Public — thống kê rating
    @GetMapping("/products/{productId}/summary")
    public ApiResponse<ProductRatingSummaryResponse> getProductRatingSummary(
            @PathVariable Long productId) {
        return ApiResponse.<ProductRatingSummaryResponse>builder()
                .result(reviewService.getProductRatingSummary(productId))
                .build();
    }

    // POST /api/v1/reviews/{reviewId}/reply
    // Chỉ SELLER sở hữu sản phẩm mới được reply
    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<ReviewResponse> replyToReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReplyReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .message("Reply posted successfully")
                .result(reviewService.replyToReview(jwt.getSubject(), reviewId, request))
                .build();
    }
}
