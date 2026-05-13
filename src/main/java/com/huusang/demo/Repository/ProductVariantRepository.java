package com.huusang.demo.Repository;

import com.huusang.demo.Entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Tìm variant thuộc đúng product — validate variant hợp lệ
    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    // Optimistic lock — đọc variant với version check để tránh race condition
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
    Optional<ProductVariant> findByIdWithOptimisticLock(@Param("id") Long id);
}
