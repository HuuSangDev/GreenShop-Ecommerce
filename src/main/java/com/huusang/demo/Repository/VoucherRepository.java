package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    // Voucher của 1 shop
    List<Voucher> findByShopId(Long shopId);

    // Voucher user có thể dùng:
    // - active = true
    // - còn trong thời hạn
    // - còn lượt dùng (maxUsage null = unlimited)
    // - đủ điều kiện đơn tối thiểu
    // - user chưa dùng
    // - toàn sàn (shop null) HOẶC của shop cụ thể
    @Query("""
            SELECT v FROM Voucher v
            WHERE v.active = true
              AND v.startsAt <= :now
              AND v.expiresAt >= :now
              AND (v.maxUsage IS NULL OR v.usedCount < v.maxUsage)
              AND v.minOrderAmt <= :orderAmt
              AND (v.shop IS NULL OR (:#{#shopIds == null || #shopIds.isEmpty()} = false AND v.shop.id IN :shopIds))
              AND NOT EXISTS (
                  SELECT vu FROM VoucherUsage vu
                  WHERE vu.voucher = v AND vu.user.id = :userId
              )
            ORDER BY v.expiresAt ASC
            """)
    List<Voucher> findAvailableVouchers(
            @Param("now") LocalDateTime now,
            @Param("orderAmt") BigDecimal orderAmt,
            @Param("shopIds") List<Long> shopIds,
            @Param("userId") String userId
    );

    @Query("""
            SELECT v FROM Voucher v
            WHERE v.active = true
            ORDER BY v.expiresAt ASC
            """)
    List<Voucher> findAllActiveVouchers();
}