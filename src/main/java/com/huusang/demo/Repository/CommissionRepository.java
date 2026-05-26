package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, String> {

    /**
     * Tìm commission theo shop_order_id (1-1 relationship)
     */
    Optional<Commission> findByShopOrderId(Long shopOrderId);

    /**
     * Lấy tất cả commissions của một shop, sắp xếp theo thời gian mới nhất
     */
    @Query("SELECT c FROM Commission c " +
           "JOIN c.shopOrder so " +
           "JOIN so.order o " +
           "WHERE c.shop.id = :shopId " +
           "ORDER BY o.createdAt DESC")
    List<Commission> findByShopIdOrderByCreatedAtDesc(@Param("shopId") Long shopId);

    /**
     * Tính tổng commission sàn đã thu trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(c.commissionAmt), 0) FROM Commission c " +
           "JOIN c.shopOrder so " +
           "JOIN so.order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumCommissionAmountBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng commission sàn đã thu (toàn bộ)
     */
    @Query("SELECT COALESCE(SUM(c.commissionAmt), 0) FROM Commission c")
    BigDecimal sumTotalCommissionAmount();

    /**
     * Kiểm tra xem shop_order đã có commission chưa (tránh tính 2 lần)
     */
    boolean existsByShopOrderId(Long shopOrderId);
}
