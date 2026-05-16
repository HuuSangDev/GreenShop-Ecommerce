package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
