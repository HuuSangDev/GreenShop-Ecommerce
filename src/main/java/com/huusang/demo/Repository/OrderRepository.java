package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Order;
import com.huusang.demo.Enum.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy tất cả đơn hàng của 1 buyer (theo email/user id)
    List<Order> findByBuyerIdOrderByCreatedAtDesc(String buyerId);

    // ─── ADMIN QUERIES ────────────────────────────────────────────────────────

    /**
     * ADMIN: Lọc đơn hàng theo status (null = tất cả), hỗ trợ phân trang.
     * Keyword tìm theo buyerEmail hoặc buyerFullName (null = bỏ qua).
     */
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.buyer b
            WHERE (:status IS NULL OR o.status = :status)
              AND (:keyword IS NULL OR LOWER(b.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findAdminOrders(
            @Param("status") OrderStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /** ADMIN: Đếm số đơn hàng mới từ thời điểm `since` đến hiện tại */
    long countByCreatedAtAfter(LocalDateTime since);

    /**
     * ADMIN: Tính tổng GMV (Gross Merchandise Value) — tổng finalAmount của tất cả
     * đơn hàng đã xác nhận (bỏ qua CANCELLED và PENDING_PAYMENT).
     */
    @Query("""
            SELECT COALESCE(SUM(o.finalAmount), 0)
            FROM Order o
            WHERE o.status NOT IN ('CANCELLED', 'PENDING_PAYMENT')
            """)
    BigDecimal sumGmv();

    /**
     * ADMIN: Tổng doanh thu theo ngày (DAILY) — dùng để vẽ Revenue Line Chart.
     * Trả về mảng [ngày, tổng] theo dạng Object[].
     */
    @Query(value = """
            SELECT DATE(o.created_at) AS period, COALESCE(SUM(o.final_amount), 0) AS revenue
            FROM orders o
            WHERE o.status NOT IN ('CANCELLED', 'PENDING_PAYMENT')
              AND o.created_at >= :since
            GROUP BY DATE(o.created_at)
            ORDER BY period ASC
            """, nativeQuery = true)
    List<Object[]> sumRevenueGroupByDay(@Param("since") LocalDateTime since);

    /**
     * ADMIN: Tổng doanh thu theo tháng (MONTHLY).
     */
    @Query(value = """
            SELECT DATE_FORMAT(o.created_at, '%Y-%m') AS period, COALESCE(SUM(o.final_amount), 0) AS revenue
            FROM orders o
            WHERE o.status NOT IN ('CANCELLED', 'PENDING_PAYMENT')
              AND o.created_at >= :since
            GROUP BY DATE_FORMAT(o.created_at, '%Y-%m')
            ORDER BY period ASC
            """, nativeQuery = true)
    List<Object[]> sumRevenueGroupByMonth(@Param("since") LocalDateTime since);

    /**
     * ADMIN: Tổng doanh thu theo năm (YEARLY).
     */
    @Query(value = """
            SELECT YEAR(o.created_at) AS period, COALESCE(SUM(o.final_amount), 0) AS revenue
            FROM orders o
            WHERE o.status NOT IN ('CANCELLED', 'PENDING_PAYMENT')
              AND o.created_at >= :since
            GROUP BY YEAR(o.created_at)
            ORDER BY period ASC
            """, nativeQuery = true)
    List<Object[]> sumRevenueGroupByYear(@Param("since") LocalDateTime since);
}

