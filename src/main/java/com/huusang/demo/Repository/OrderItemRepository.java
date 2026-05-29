package com.huusang.demo.Repository;

import com.huusang.demo.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Lấy order item kèm thông tin order để validate ownership + status
    @Query("""
            SELECT oi FROM OrderItem oi
            JOIN FETCH oi.shopOrder so
            JOIN FETCH so.order o
            JOIN FETCH o.buyer b
            JOIN FETCH oi.productVariant pv
            JOIN FETCH pv.product p
            WHERE oi.id = :orderItemId
            """)
    Optional<OrderItem> findByIdWithOrderDetails(@Param("orderItemId") Long orderItemId);
}
