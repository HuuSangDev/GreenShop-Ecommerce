package com.huusang.demo.Controller;

import com.huusang.demo.Dto.ApiResponse;
import com.huusang.demo.Dto.Request.CreateReviewRequest;
import com.huusang.demo.Dto.Request.UpdateReviewRequest;
import com.huusang.demo.Dto.Response.*;
import com.huusang.demo.Service.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1")
public class ReviewController {

    ReviewService reviewService;

    // ─── CREATE REVIEW ────────────────────────────────────────────────────────
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse review = reviewService.createReview(getUserEmail(jwt), request);

        return ApiResponse.<ReviewResponse>builder()
                .code(201)
                .message("Đánh giá thành công")
                .result(review)
                .build();
    }

    // ─── UPDATE REVIEW ────────────────────────────────────────────────────────
    @PutMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {

        ReviewResponse review = reviewService.updateReview(getUserEmail(jwt), reviewId, request);

        return ApiResponse.<ReviewResponse>builder()
                .code(200)
                .message("Cập nhật đánh giá thành công")
                .result(review)
                .build();
    }

    // ─── DELETE REVIEW ────────────────────────────────────────────────────────
    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<String> deleteReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reviewId) {

        reviewService.deleteReview(getUserEmail(jwt), reviewId);

        return ApiResponse.<String>builder()
                .code(200)
                .message("Xóa đánh giá thành công")
                .build();
    }

    // ─── GET PRODUCT REVIEWS ──────────────────────────────────────────────────
    @GetMapping("/products/{productId}/reviews")
    public ApiResponse<Object> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, rating, pageable);
        ReviewStatsResponse stats = reviewService.getProductReviewStats(productId);

        return ApiResponse.builder()
                .code(200)
                .message("Danh sách đánh giá")
                .result(new Object() {
                    public Page<ReviewResponse> getContent() { return reviews; }
                    public ReviewStatsResponse getStats() { return stats; }
                })
                .build();
    }

    // ─── GET USER REVIEWS ─────────────────────────────────────────────────────
    @GetMapping("/users/{userId}/reviews")
    public ApiResponse<Page<ReviewDetailResponse>> getUserReviews(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReviewDetailResponse> reviews = reviewService.getUserReviews(userId, pageable);

        return ApiResponse.<Page<ReviewDetailResponse>>builder()
                .code(200)
                .message("Danh sách đánh giá của user")
                .result(reviews)
                .build();
    }

    // ─── GET REVIEW DETAIL ────────────────────────────────────────────────────
    @GetMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewDetailResponse> getReviewDetail(@PathVariable Long reviewId) {
        ReviewDetailResponse review = reviewService.getReviewDetail(reviewId);

        return ApiResponse.<ReviewDetailResponse>builder()
                .code(200)
                .message("Chi tiết đánh giá")
                .result(review)
                .build();
    }

    // ─── GET USER'S REVIEW FOR PRODUCT ────────────────────────────────────────
    @GetMapping("/products/{productId}/reviews/my-review")
    public ApiResponse<ReviewResponse> getUserProductReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId) {

        ReviewResponse review = reviewService.getUserProductReview(getUserEmail(jwt), productId);

        return ApiResponse.<ReviewResponse>builder()
                .code(200)
                .message("Review của bạn")
                .result(review)
                .build();
    }

    // ─── GET PENDING REVIEWS ──────────────────────────────────────────────────
    @GetMapping("/reviews/pending")
    public ApiResponse<List<PendingReviewResponse>> getPendingReviews(
            @AuthenticationPrincipal Jwt jwt) {

        List<PendingReviewResponse> pending = reviewService.getPendingReviews(getUserEmail(jwt));

        return ApiResponse.<List<PendingReviewResponse>>builder()
                .code(200)
                .message("Danh sách sản phẩm chờ đánh giá")
                .result(pending)
                .build();
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────
    private String getUserEmail(Jwt jwt) {
        return jwt.getClaimAsString("sub");
    }
}
