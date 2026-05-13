package com.huusang.demo.Repository;

<<<<<<< HEAD
import com.huusang.demo.Entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
=======
import com.huusang.demo.Entity.Product;
import com.huusang.demo.Entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
>>>>>>> 2fcffb4418523408e449bdcfc70909241c4c77c4
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> 2fcffb4418523408e449bdcfc70909241c4c77c4
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
<<<<<<< HEAD

    // Tìm variant thuộc đúng product — validate variant hợp lệ
    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    // Optimistic lock — đọc variant với version check để tránh race condition
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id = :id")
    Optional<ProductVariant> findByIdWithOptimisticLock(@Param("id") Long id);
=======
    
    // Tìm tất cả variant của 1 sản phẩm
    List<ProductVariant> findByProduct(Product product);
    
    // Tìm variant theo product ID
    List<ProductVariant> findByProductId(Long productId);
    
    // Kiểm tra variant có trong đơn hàng active không
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE oi.product.id = :productId " +
           "AND o.status IN ('PENDING', 'PROCESSING', 'SHIPPED')")
    boolean isVariantInActiveOrder(@Param("productId") Long productId);
    
    // Tìm variant theo SKU
    Optional<ProductVariant> findBySku(String sku);
>>>>>>> 2fcffb4418523408e449bdcfc70909241c4c77c4
}
