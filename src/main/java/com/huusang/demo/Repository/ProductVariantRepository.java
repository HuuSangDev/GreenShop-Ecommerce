package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Product;
import com.huusang.demo.Entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
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
}
