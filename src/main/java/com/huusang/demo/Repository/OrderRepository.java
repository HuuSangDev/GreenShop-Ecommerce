package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy tất cả đơn hàng của 1 buyer (theo email/user id)
    List<Order> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
}
