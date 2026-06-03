package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, String> {

    // Kiểm tra commission đã được tạo cho shopOrder này chưa (idempotency)
    Optional<Commission> findByShopOrderId(Long shopOrderId);

    // Tổng commission của 1 shop (admin xem)
    @Query("SELECT COALESCE(SUM(c.commissionAmt), 0) FROM Commission c WHERE c.shop.id = :shopId")
    BigDecimal sumCommissionByShopId(@Param("shopId") Long shopId);

    // Lấy tất cả commission (admin dashboard)
    List<Commission> findAllByOrderByShopOrderIdDesc();
}
