package com.huusang.demo.Repository;

import com.huusang.demo.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    // Tìm item theo cart + variant — dùng để check duplicate khi addToCart
    Optional<CartItem> findByCartIdAndProductVariantId(String cartId, Long variantId);

    // Lấy tất cả items với JOIN FETCH để tránh N+1 query
    @Query("""
            SELECT ci FROM CartItem ci
            JOIN FETCH ci.product p
            JOIN FETCH ci.productVariant pv
            WHERE ci.cart.id = :cartId
            ORDER BY ci.addedAt DESC
            """)
    List<CartItem> findByCartIdWithDetails(@Param("cartId") String cartId);

    // Xóa toàn bộ items của cart (clearCart)
    void deleteByCartId(String cartId);
}
