package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Product;
import com.huusang.demo.Entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByShopAndAvailableTrue(Shop shop, Pageable pageable);

    Optional<Product> findByIdAndAvailableTrue(Long id);

    @Query("""
           SELECT p FROM Product p
           WHERE p.available = true
           AND (
               LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
           )
           """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
           SELECT p FROM Product p
           WHERE p.available = true
           AND (:categoryId IS NULL OR p.category.id = :categoryId)
           AND (:shopId IS NULL OR p.shop.id = :shopId)
           AND (:minPrice IS NULL OR p.price >= :minPrice)
           AND (:maxPrice IS NULL OR p.price <= :maxPrice)
           """)
    Page<Product> filterProducts(
            @Param("categoryId") Long categoryId,
            @Param("shopId") Long shopId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
     * Lọc sản phẩm theo danh sách categoryIds (cha + các con).
     * Dùng mệnh đề IN để bao gồm tất cả sản phẩm thuộc mọi danh mục con.
     * categoryIds = null hoặc rỗng → không lọc theo danh mục.
     */
    @Query("""
           SELECT p FROM Product p
           WHERE p.available = true
           AND (:#{#categoryIds == null || #categoryIds.isEmpty()} = true OR p.category.id IN :categoryIds)
           AND (:shopId IS NULL OR p.shop.id = :shopId)
           AND (:minPrice IS NULL OR p.price >= :minPrice)
           AND (:maxPrice IS NULL OR p.price <= :maxPrice)
           AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                 OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Product> filterProductsByCategories(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("shopId")      Long shopId,
            @Param("minPrice")    BigDecimal minPrice,
            @Param("maxPrice")    BigDecimal maxPrice,
            @Param("keyword")     String keyword,
            Pageable pageable
    );


    Page<Product> findByAvailableTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
           SELECT p FROM Product p
           JOIN OrderItem oi ON p.id = oi.productVariant.product.id
           WHERE p.available = true
           GROUP BY p
           ORDER BY SUM(oi.quantity) DESC
           """)
    Page<Product> findTopSellingProducts(Pageable pageable);

    @Query("""
           SELECT p FROM Product p
           JOIN OrderItem oi ON p.id = oi.productVariant.product.id
           WHERE p.available = true
           AND p.shop.id = :shopId
           GROUP BY p
           ORDER BY SUM(oi.quantity) DESC
           """)
    Page<Product> findTopSellingProductsByShop(
            @Param("shopId") Long shopId,
            Pageable pageable
    );


    // Admin: Tất cả sản phẩm (kể cả ẩn)
    Page<Product> findAll(Pageable pageable);

    // Admin: Lấy sản phẩm theo trạng thái hidden
    @Query("""
           SELECT p FROM Product p
           WHERE p.hidden = :hidden
           ORDER BY p.createdAt DESC
           """)
    Page<Product> findByHidden(
            @Param("hidden") boolean hidden,
            Pageable pageable
    );

    // Admin: Lấy sản phẩm không bị ẩn (available = true và hidden = false)
    @Query("""
           SELECT p FROM Product p
           WHERE p.hidden = false
           ORDER BY p.createdAt DESC
           """)
    Page<Product> findByHiddenFalse(Pageable pageable);
}
