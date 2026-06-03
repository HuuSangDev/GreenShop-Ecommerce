package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Dùng trong SePay webhook callback:
     * SePay gửi lại transaction_ref (= nội dung chuyển khoản "ORDER_{id}")
     * → tìm payment tương ứng để update status.
     */
    Optional<Payment> findByTransactionRef(String transactionRef);

    @Query("SELECT p FROM Payment p JOIN p.order o JOIN o.shopOrders so WHERE so.shop.id = :shopId ORDER BY p.createdAt DESC")
    List<Payment> findByShopId(@Param("shopId") Long shopId);
}

