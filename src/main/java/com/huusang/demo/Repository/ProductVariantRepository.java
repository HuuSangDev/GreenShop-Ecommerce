package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Product;
import com.huusang.demo.Entity.ProductVariant;
import com.huusang.demo.Enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct(Product product);

    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    // Kiểm tra variant có trong đơn hàng active không
    // OrderItem → shopOrder → status
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
           "FROM OrderItem oi JOIN oi.shopOrder so " +
           "WHERE oi.productVariant.id = :variantId " +
           "AND so.status IN :activeStatuses")
    boolean isVariantInActiveOrder(
            @Param("variantId") Long variantId,
            @Param("activeStatuses") List<OrderStatus> activeStatuses
    );
}
