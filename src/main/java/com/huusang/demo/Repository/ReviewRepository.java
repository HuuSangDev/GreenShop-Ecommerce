package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Kiểm tra order_item đã được review chưa (chặn duplicate)
    boolean existsByOrderItemIdAndIsDeletedFalse(Long orderItemId);

    // Lấy review theo id, chưa bị xóa
    Optional<Review> findByIdAndIsDeletedFalse(Long id);

    // Lấy danh sách review của sản phẩm (public — chỉ lấy chưa bị xóa)
    // Hỗ trợ sort: createdAt DESC, rating DESC, rating ASC qua Pageable
    @Query("""
            SELECT r FROM Review r
            JOIN FETCH r.user u
            LEFT JOIN FETCH r.repliedByShop s
            WHERE r.product.id = :productId
            AND r.isDeleted = false
            """)
    Page<Review> findByProductIdAndNotDeleted(
            @Param("productId") Long productId,
            Pageable pageable);

    // Tính rating summary — 1 query duy nhất, tối ưu cho số lượng lớn
    @Query("""
            SELECT r.rating, COUNT(r)
            FROM Review r
            WHERE r.product.id = :productId
            AND r.isDeleted = false
            GROUP BY r.rating
            """)
    List<Object[]> countByRatingForProduct(@Param("productId") Long productId);

    // Tính average và total — dùng để recalculate sau create/update/delete
    @Query("""
            SELECT AVG(r.rating), COUNT(r)
            FROM Review r
            WHERE r.product.id = :productId
            AND r.isDeleted = false
            """)
    Object[] calculateRatingSummary(@Param("productId") Long productId);
}
