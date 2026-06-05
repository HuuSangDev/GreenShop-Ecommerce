package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Order;
import com.huusang.demo.Enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy tất cả đơn hàng của 1 buyer
    List<Order> findByBuyer_IdOrderByCreatedAtDesc(String buyerId);

    // Lấy đơn hàng theo trạng thái
    List<Order> findByBuyer_IdAndStatusOrderByCreatedAtDesc(String buyerId, OrderStatus status);

    // Admin: Lấy tất cả đơn hàng
    List<Order> findAllByOrderByCreatedAtDesc();

    // Admin: Lấy đơn hàng theo trạng thái
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    // Dọn dẹp dữ liệu rác/lỗi ở cột status để tránh lỗi No enum constant
    @Modifying
    @Transactional
    @Query(value = "UPDATE orders SET status = 'PENDING' WHERE status IS NULL OR status = '' OR status NOT IN ('PENDING', 'PENDING_PAYMENT', 'PAID', 'PREPARING', 'READY_TO_SHIP', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED')", nativeQuery = true)
    void sanitizeStatuses();
}
