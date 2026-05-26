package com.huusang.demo.Repository;

import com.huusang.demo.Entity.OrderItem;
import com.huusang.demo.Enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Lấy tất cả order items của 1 shop order
    List<OrderItem> findByShopOrderId(Long shopOrderId);

    /**
     * Lấy tất cả OrderItems của 1 Order (qua ShopOrder) — dùng trong SePay webhook
     * để biết cần deduct stock cho những variant nào.
     * JOIN FETCH productVariant để tránh N+1 khi truy cập variant.getId() / getStockQuantity().
     */
    @Query("""
            SELECT oi FROM OrderItem oi
            JOIN FETCH oi.productVariant pv
            JOIN oi.shopOrder so
            WHERE so.order.id = :orderId
            """)
    List<OrderItem> findByShopOrderOrderId(@Param("orderId") Long orderId);

    /**
     * Lấy các OrderItems đã DELIVERED của 1 shop — dùng để tính doanh thu thực tế.
     * JOIN FETCH để tránh N+1 khi truy cập productVariant.product và shopOrder.order.
     */
    @Query("""
            SELECT oi FROM OrderItem oi
            JOIN FETCH oi.productVariant pv
            JOIN FETCH pv.product p
            JOIN FETCH oi.shopOrder so
            JOIN FETCH so.order o
            WHERE so.shop.id = :shopId
              AND so.status = com.huusang.demo.Enum.OrderStatus.DELIVERED
            """)
    List<OrderItem> findDeliveredItemsByShopId(@Param("shopId") Long shopId);
}

