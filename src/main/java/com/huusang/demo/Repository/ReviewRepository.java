package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tìm review theo product & user
    Optional<Review> findByProductIdAndUserId(Long productId, String userId);

    // Tìm review theo orderItem
    Optional<Review> findByOrderItemId(Long orderItemId);

    // Lấy review của sản phẩm (phân trang)
    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    // Lấy review của sản phẩm theo rating
    Page<Review> findByProductIdAndRatingOrderByCreatedAtDesc(Long productId, Integer rating, Pageable pageable);

    // Lấy review của user
    Page<Review> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Kiểm tra user đã review sản phẩm chưa
    boolean existsByProductIdAndUserId(Long productId, String userId);

    // Tính trung bình rating
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    BigDecimal getAverageRating(@Param("productId") Long productId);

    // Đếm review
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Integer countReviewsByProduct(@Param("productId") Long productId);

    // Thống kê rating distribution
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);
}
