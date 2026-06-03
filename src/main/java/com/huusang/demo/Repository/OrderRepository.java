package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Order;
import com.huusang.demo.Enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
