package com.huusang.demo.Repository;

import com.huusang.demo.Entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    // Lấy tất cả shop orders của 1 đơn hàng gốc
    List<ShopOrder> findByOrderId(Long orderId);

    // Lấy tất cả shop orders của 1 shop (cho seller dashboard)
    List<ShopOrder> findByShopIdOrderByIdDesc(Long shopId);
}
