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

    // Lấy danh sách cart items theo IDs kèm JOIN FETCH để dùng trong checkout — tránh N+1
    @Query("""
            SELECT ci FROM CartItem ci
            JOIN FETCH ci.productVariant pv
            JOIN FETCH ci.product p
            JOIN FETCH p.shop s
            JOIN FETCH ci.cart c
            JOIN FETCH c.user u
            WHERE ci.id IN :ids
            """)
    List<CartItem> findByIdInWithDetails(@Param("ids") List<String> ids);

    /**
     * Dùng trong SePay webhook để xóa cart items sau khi payment thành công.
     * Tìm tất cả items trong cart của buyer có variant thuộc danh sách đã mua.
     * Trả về Optional<List> — empty nếu giỏ hàng đã được clear trước đó.
     */
    @Query("""
            SELECT ci FROM CartItem ci
            JOIN FETCH ci.cart c
            WHERE c.user.id = :userId
            AND ci.productVariant.id IN :variantIds
            """)
    Optional<List<CartItem>> findByCartUserIdAndVariantIds(
            @Param("userId") String userId,
            @Param("variantIds") List<Long> variantIds);
}

