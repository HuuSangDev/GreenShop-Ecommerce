package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Product;
import com.huusang.demo.Entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Tìm variant thuộc đúng product — validate variant hợp lệ
    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    // Optimistic lock — đọc variant với version check để tránh race condition (dùng cho addToCart)
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
    Optional<ProductVariant> findByIdWithOptimisticLock(@Param("id") Long id);

    // ─── Pessimistic lock — dùng khi checkout để khóa row tránh overselling ───────
    // Thực thi: SELECT * FROM product_variants WHERE id = ? FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);

    // Trừ tồn kho sau khi checkout (trong cùng transaction đã giữ lock)
    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :quantity WHERE pv.id = :id")
    void deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    // Tìm tất cả variant của 1 sản phẩm
    List<ProductVariant> findByProduct(Product product);

    // Tìm variant theo product ID
    List<ProductVariant> findByProductId(Long productId);

    // Tìm variant theo SKU
    Optional<ProductVariant> findBySku(String sku);

    // Kiểm tra variant có trong đơn hàng active không (chặn xóa variant)
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "WHERE oi.productVariant.id = :variantId " +
           "AND oi.shopOrder.order.status NOT IN ('COMPLETED', 'CANCELLED')")
    boolean isVariantInActiveOrder(@Param("variantId") Long variantId);
}
