package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.*;
import com.huusang.demo.Dto.Response.ProductRatingSummaryResponse;
import com.huusang.demo.Dto.Response.ReviewResponse;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {

    ReviewRepository    reviewRepository;
    OrderItemRepository orderItemRepository;
    ProductRepository   productRepository;
    UserRepository      userRepository;

    static int REVIEW_EDIT_DAYS = 7;

    // ─── 1. CREATE REVIEW ────────────────────────────────────────────────────
    @Transactional
    public ReviewResponse createReview(String userEmail, CreateReviewRequest request) {
        User reviewer = findUserByEmail(userEmail);

        // Lấy order item kèm đầy đủ thông tin để validate
        OrderItem orderItem = orderItemRepository
                .findByIdWithOrderDetails(request.getOrderItemId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_ORDER_ITEM_NOT_FOUND));

        // BR1: order_item phải thuộc về user hiện tại
        Order order = orderItem.getShopOrder().getOrder();
        if (!order.getBuyer().getId().equals(reviewer.getId())) {
            throw new AppException(ErrorCode.REVIEW_ORDER_ITEM_NOT_OWNED);
        }

        // BR2: ShopOrder status phải là COMPLETED
        // (dùng ShopOrder status vì mỗi shop có trạng thái độc lập)
        if (orderItem.getShopOrder().getStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.REVIEW_ORDER_NOT_COMPLETED);
        }

        // BR3: order_item chưa được review trước đó
        if (reviewRepository.existsByOrderItemIdAndIsDeletedFalse(request.getOrderItemId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Product product = orderItem.getProductVariant().getProduct();

        Review review = Review.builder()
                .product(product)
                .user(reviewer)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>())
                .isDeleted(false)
                .build();

        Review saved = reviewRepository.save(review);

        // Cập nhật rating summary của sản phẩm
        recalculateProductRating(product.getId());

        log.info("Review created: reviewId={}, productId={}, userId={}, rating={}",
                saved.getId(), product.getId(), reviewer.getId(), request.getRating());

        return toResponse(saved);
    }

    // ─── 2. UPDATE REVIEW ────────────────────────────────────────────────────
    @Transactional
    public ReviewResponse updateReview(String userEmail, Long reviewId, UpdateReviewRequest request) {
        User reviewer = findUserByEmail(userEmail);
        Review review = findActiveReviewOrThrow(reviewId);

        // BR1: chỉ owner mới được sửa
        if (!review.getUser().getId().equals(reviewer.getId())) {
            throw new AppException(ErrorCode.REVIEW_NOT_OWNER);
        }

        // BR2: chỉ được sửa trong vòng 7 ngày kể từ lúc tạo
        LocalDateTime editDeadline = review.getCreatedAt().plusDays(REVIEW_EDIT_DAYS);
        if (LocalDateTime.now().isAfter(editDeadline)) {
            throw new AppException(ErrorCode.REVIEW_EDIT_EXPIRED);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        if (request.getImageUrls() != null) {
            review.setImageUrls(request.getImageUrls());
        }

        Review saved = reviewRepository.save(review);

        // Cập nhật lại rating summary vì rating có thể thay đổi
        recalculateProductRating(review.getProduct().getId());

        log.info("Review updated: reviewId={}, userId={}", reviewId, reviewer.getId());

        return toResponse(saved);
    }

    // ─── 3. DELETE REVIEW (Admin only — soft delete) ─────────────────────────
    @Transactional
    public void deleteReview(String adminEmail, Long reviewId, DeleteReviewRequest request) {
        User admin = findUserByEmail(adminEmail);
        Review review = findActiveReviewOrThrow(reviewId);

        review.setIsDeleted(true);
        review.setDeletedAt(LocalDateTime.now());
        review.setDeletedByAdmin(admin);
        review.setDeleteReason(request.getReason());

        reviewRepository.save(review);

        // Cập nhật lại rating summary sau khi xóa
        recalculateProductRating(review.getProduct().getId());

        log.info("Review soft-deleted: reviewId={}, adminId={}, reason={}",
                reviewId, admin.getId(), request.getReason());
    }

    // ─── 4. GET REVIEWS BY PRODUCT ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProduct(Long productId, int page, int size, String sortBy) {
        // Validate product tồn tại
        productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Sort sort = switch (sortBy) {
            case "highest_rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "lowest_rating"  -> Sort.by(Sort.Direction.ASC,  "rating");
            default               -> Sort.by(Sort.Direction.DESC, "createdAt"); // newest
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviews = reviewRepository.findByProductIdAndNotDeleted(productId, pageable);
        return reviews.map(this::toResponse);
    }

    // ─── 5. GET PRODUCT RATING SUMMARY ───────────────────────────────────────
    @Transactional(readOnly = true)
    public ProductRatingSummaryResponse getProductRatingSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // 1 query lấy count theo từng mức sao
        List<Object[]> rawCounts = reviewRepository.countByRatingForProduct(productId);

        Map<Integer, Long>   starCounts      = new LinkedHashMap<>();
        Map<Integer, Double> starPercentages = new LinkedHashMap<>();

        // Khởi tạo tất cả 5 mức sao = 0
        for (int i = 1; i <= 5; i++) {
            starCounts.put(i, 0L);
        }

        long total = 0;
        for (Object[] row : rawCounts) {
            int  star  = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            starCounts.put(star, count);
            total += count;
        }

        // Tính phần trăm
        for (int i = 1; i <= 5; i++) {
            double pct = total > 0
                    ? Math.round((starCounts.get(i) * 100.0 / total) * 10.0) / 10.0
                    : 0.0;
            starPercentages.put(i, pct);
        }

        return ProductRatingSummaryResponse.builder()
                .productId(productId)
                .productName(product.getProductName())
                .averageRating(product.getAverageRating())
                .totalReviews(product.getTotalReviews())
                .starCounts(starCounts)
                .starPercentages(starPercentages)
                .build();
    }

    // ─── 6. REPLY TO REVIEW (Seller only) ────────────────────────────────────
    @Transactional
    public ReviewResponse replyToReview(String sellerEmail, Long reviewId, ReplyReviewRequest request) {
        User seller = findUserByEmail(sellerEmail);
        Review review = findActiveReviewOrThrow(reviewId);

        // BR1: review đã có reply rồi thì không cho reply lại
        if (review.getSellerReply() != null) {
            throw new AppException(ErrorCode.REVIEW_REPLY_ALREADY_EXISTS);
        }

        // BR2: chỉ seller sở hữu shop của sản phẩm mới được reply
        Shop productShop = review.getProduct().getShop();
        if (productShop == null || !productShop.getOwner().getId().equals(seller.getId())) {
            throw new AppException(ErrorCode.REVIEW_REPLY_NOT_SHOP_OWNER);
        }

        review.setSellerReply(request.getReply());
        review.setSellerRepliedAt(LocalDateTime.now());
        review.setRepliedByShop(productShop);

        Review saved = reviewRepository.save(review);

        log.info("Seller replied to review: reviewId={}, shopId={}", reviewId, productShop.getId());

        return toResponse(saved);
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    // Recalculate và persist rating summary vào Product
    // Gọi sau mỗi create/update/delete review để giữ dữ liệu đồng bộ
    private void recalculateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Object[] result = reviewRepository.calculateRatingSummary(productId);
        Double avg   = result[0] != null ? ((Number) result[0]).doubleValue() : 0.0;
        Long   count = result[1] != null ? ((Number) result[1]).longValue()   : 0L;

        product.setAverageRating(
                BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
        );
        product.setTotalReviews(count.intValue());
        productRepository.save(product);
    }

    private Review findActiveReviewOrThrow(Long reviewId) {
        return reviewRepository.findByIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getProductName())
                .userId(r.getUser().getId())
                .reviewerName(
                        r.getUser().getFullName() != null
                                ? r.getUser().getFullName()
                                : r.getUser().getUsername()
                )
                .orderItemId(r.getOrderItem().getId())
                .variantName(r.getOrderItem().getProductVariant().getVariantName())
                .rating(r.getRating())
                .comment(r.getComment())
                .imageUrls(r.getImageUrls())
                .sellerReply(r.getSellerReply())
                .sellerRepliedAt(r.getSellerRepliedAt())
                .shopName(r.getRepliedByShop() != null ? r.getRepliedByShop().getShopName() : null)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
