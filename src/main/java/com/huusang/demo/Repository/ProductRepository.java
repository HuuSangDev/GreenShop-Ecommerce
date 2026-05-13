package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Product;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
=======
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
    
    // Tìm sản phẩm theo shop và available = true
    Page<Product> findByShopAndAvailableTrue(Shop shop, Pageable pageable);
    
    // Tìm sản phẩm theo ID và available = true
    Optional<Product> findByIdAndAvailableTrue(Long id);
    
    // Tìm kiếm theo keyword
    @Query("SELECT p FROM Product p WHERE p.available = true AND " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Lọc theo nhiều tiêu chí
    @Query("SELECT p FROM Product p WHERE p.available = true " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:shopId IS NULL OR p.shop.id = :shopId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> filterProducts(
            @Param("categoryId") Long categoryId,
            @Param("shopId") Long shopId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    
    // Sản phẩm mới nhất
    Page<Product> findByAvailableTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Sản phẩm bán chạy (cần join với OrderItem)
    @Query("SELECT p FROM Product p JOIN OrderItem oi ON p.id = oi.product.id " +
           "WHERE p.available = true " +
           "GROUP BY p.id ORDER BY SUM(oi.quantity) DESC")
    Page<Product> findTopSellingProducts(Pageable pageable);
    
    // Sản phẩm bán chạy theo shop
    @Query("SELECT p FROM Product p JOIN OrderItem oi ON p.id = oi.product.id " +
           "WHERE p.available = true AND p.shop.id = :shopId " +
           "GROUP BY p.id ORDER BY SUM(oi.quantity) DESC")
    Page<Product> findTopSellingProductsByShop(@Param("shopId") Long shopId, Pageable pageable);
    
    // Admin: Tất cả sản phẩm (kể cả ẩn)
    Page<Product> findAll(Pageable pageable);
>>>>>>> 2fcffb4418523408e449bdcfc70909241c4c77c4
}
