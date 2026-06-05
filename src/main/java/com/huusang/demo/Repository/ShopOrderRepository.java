package com.huusang.demo.Repository;
 
import com.huusang.demo.Entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
 
    // Lấy tất cả shop orders của 1 đơn hàng gốc
    List<ShopOrder> findByOrderId(Long orderId);
 
    // Lấy tất cả shop orders của 1 shop (cho seller dashboard)
    List<ShopOrder> findByShopIdOrderByIdDesc(Long shopId);

    // Dọn dẹp dữ liệu rác/lỗi ở cột status để tránh lỗi No enum constant
    @Modifying
    @Transactional
    @Query(value = "UPDATE shop_orders SET status = 'PENDING' WHERE status IS NULL OR status = '' OR status NOT IN ('PENDING', 'PENDING_PAYMENT', 'PAID', 'PREPARING', 'READY_TO_SHIP', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED')", nativeQuery = true)
    void sanitizeStatuses();
}
