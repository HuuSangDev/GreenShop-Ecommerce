package com.huusang.demo.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huusang.demo.Dto.Request.CreateReviewRequest;
import com.huusang.demo.Dto.Request.UpdateReviewRequest;
import com.huusang.demo.Dto.Response.*;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {

    ReviewRepository reviewRepository;
    OrderItemRepository orderItemRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    ShopRepository shopRepository;
    FileStorageService fileStorageService;
    ObjectMapper objectMapper;
    NotificationService notificationService;

    // ─── CREATE ───────────────────────────────────────────────────────────────
    @Transactional
    public ReviewResponse createReview(String userEmail, CreateReviewRequest request,
                                       List<MultipartFile> images) {
        User user = getUserByEmail(userEmail);
        OrderItem orderItem = getOrderItemOrThrow(request.getOrderItemId());

        // Validate OrderItem belongs to user
        if (!orderItem.getShopOrder().getOrder().getBuyer().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_CART_ITEM_NOT_OWNED);
        }

        // Validate Order status = DELIVERED
        Order order = orderItem.getShopOrder().getOrder();
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.REVIEW_ORDER_NOT_DELIVERED);
        }

        // Check no existing review
        if (reviewRepository.findByOrderItemId(request.getOrderItemId()).isPresent()) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Product product = orderItem.getProductVariant().getProduct();

        // Upload ảnh nếu có
        List<String> imageUrls = uploadImages(images);

        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .imageUrlsJson(toJson(imageUrls))
                .verifiedPurchase(true)
                .build();

        reviewRepository.save(review);
        updateProductRating(product.getId());

        log.info("Review created: reviewId={}, productId={}, userId={}, rating={}, images={}",
                review.getId(), product.getId(), user.getId(), request.getRating(), imageUrls.size());

        if (product.getShop() != null && product.getShop().getOwner() != null) {
            notificationService.sendNotification(
                    product.getShop().getOwner(),
                    com.huusang.demo.Enum.NotificationType.NEW_REVIEW,
                    "Đánh giá mới",
                    "Sản phẩm " + product.getProductName() + " vừa nhận được một đánh giá " + request.getRating() + " sao.",
                    product.getId().toString()
            );
        }

        return toReviewResponse(review);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    @Transactional
    public ReviewResponse updateReview(String userEmail, Long reviewId, UpdateReviewRequest request) {
        User user = getUserByEmail(userEmail);
        Review review = getReviewOrThrow(reviewId);

        // Check authorization
        if (!review.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.REVIEW_NOT_OWNED);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        updateProductRating(review.getProduct().getId());

        log.info("Review updated: reviewId={}, rating={}", reviewId, request.getRating());

        return toReviewResponse(review);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    @Transactional
    public void deleteReview(String userEmail, Long reviewId) {
        User user = getUserByEmail(userEmail);
        Review review = getReviewOrThrow(reviewId);

        // Check authorization (owner or admin)
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));

        if (!isOwner && !isAdmin) {
            throw new AppException(ErrorCode.REVIEW_NOT_OWNED);
        }

        // Xóa những file ảnh đã upload
        deleteImages(fromJson(review.getImageUrlsJson()));

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        updateProductRating(productId);

        log.info("Review deleted: reviewId={}", reviewId);
    }

    // ─── GET ──────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Integer rating, Pageable pageable) {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Page<Review> reviews;
        if (rating != null) {
            reviews = reviewRepository.findByProductIdAndRatingOrderByCreatedAtDesc(productId, rating, pageable);
        } else {
            reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        }

        return reviews.map(this::toReviewResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDetailResponse> getUserReviews(String userId, Pageable pageable) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Page<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return reviews.map(this::toReviewDetailResponse);
    }

    @Transactional(readOnly = true)
    public ReviewDetailResponse getReviewDetail(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        return toReviewDetailResponse(review);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getUserProductReview(String userEmail, Long productId) {
        User user = getUserByEmail(userEmail);

        Review review = reviewRepository.findByProductIdAndUserId(productId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        return toReviewResponse(review);
    }

    @Transactional(readOnly = true)
    public List<PendingReviewResponse> getPendingReviews(String userEmail) {
        User user = getUserByEmail(userEmail);

        // Get all delivered orders for user
        List<Order> deliveredOrders = user.getOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        List<PendingReviewResponse> pending = new ArrayList<>();

        for (Order order : deliveredOrders) {
            for (ShopOrder shopOrder : order.getShopOrders()) {
                for (OrderItem item : shopOrder.getOrderItems()) {
                    // Check if review doesn't exist
                    if (!reviewRepository.findByOrderItemId(item.getId()).isPresent()) {
                        ProductVariant variant = item.getProductVariant();
                        Product product = variant.getProduct();
                        Shop shop = shopOrder.getShop();

                        pending.add(PendingReviewResponse.builder()
                                .orderItemId(item.getId())
                                .orderId(order.getId())
                                .productId(product.getId())
                                .productName(product.getProductName())
                                .productImage(product.getImageUrl())
                                .variantName(variant.getVariantName())
                                .shopId(shop.getId())
                                .shopName(shop.getShopName())
                                .quantity(item.getQuantity())
                                .priceAtBuy(item.getPriceAtBuy())
                                .deliveredAt(order.getCreatedAt())
                                .build());
                    }
                }
            }
        }

        return pending;
    }

    @Transactional(readOnly = true)
    public ReviewStatsResponse getProductReviewStats(Long productId) {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Product product = productRepository.findById(productId).get();

        // Get rating distribution
        List<Object[]> distribution = reviewRepository.getRatingDistribution(productId);
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (int i = 5; i >= 1; i--) {
            ratingDistribution.put(i, 0);
        }

        for (Object[] row : distribution) {
            Integer rating = ((Number) row[0]).intValue();
            Integer count = ((Number) row[1]).intValue();
            ratingDistribution.put(rating, count);
        }

        return ReviewStatsResponse.builder()
                .averageRating(product.getAverageRating())
                .totalReviews(product.getTotalReviews())
                .ratingDistribution(ratingDistribution)
                .build();
    }

    // ─── HELPER METHODS ───────────────────────────────────────────────────────
    @Transactional
    public void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        BigDecimal averageRating = reviewRepository.getAverageRating(productId);
        Integer totalReviews = reviewRepository.countReviewsByProduct(productId);

        if (averageRating == null) {
            averageRating = BigDecimal.ZERO;
        } else {
            averageRating = averageRating.setScale(2, RoundingMode.HALF_UP);
        }

        if (totalReviews == null) {
            totalReviews = 0;
        }

        product.setAverageRating(averageRating);
        product.setTotalReviews(totalReviews);
        productRepository.save(product);

        log.info("Product rating updated: productId={}, averageRating={}, totalReviews={}",
                productId, averageRating, totalReviews);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private OrderItem getOrderItemOrThrow(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_ORDER_ITEM_NOT_FOUND));
    }

    private Review getReviewOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getProductName())
                .productImage(review.getProduct().getImageUrl())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName() != null ? review.getUser().getFullName() : review.getUser().getUsername())
                .userAvatar(null)
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrls(fromJson(review.getImageUrlsJson()))
                .verifiedPurchase(review.isVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private ReviewDetailResponse toReviewDetailResponse(Review review) {
        Shop shop = review.getProduct().getShop();
        return ReviewDetailResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getProductName())
                .productImage(review.getProduct().getImageUrl())
                .shopId(shop != null ? shop.getId() : null)
                .shopName(shop != null ? shop.getShopName() : null)
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName() != null ? review.getUser().getFullName() : review.getUser().getUsername())
                .userAvatar(null)
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrls(fromJson(review.getImageUrlsJson()))
                .verifiedPurchase(review.isVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    // ─── IMAGE HELPERS ───────────────────────────────────────────────────────────
    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return List.of();
        return images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .limit(5)
                .map(f -> fileStorageService.storeFile(f, "reviews"))
                .collect(Collectors.toList());
    }

    private void deleteImages(List<String> imageUrls) {
        if (imageUrls == null) return;
        imageUrls.forEach(fileStorageService::deleteFile);
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Cannot serialize image URLs to JSON", e);
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Cannot deserialize image URLs from JSON: {}", json, e);
            return List.of();
        }
    }
}
