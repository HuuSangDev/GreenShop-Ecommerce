package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Lấy tất cả root categories của shop (không có parent)
    List<Category> findByShopIdAndParentIsNullAndIsActiveTrueOrderBySortOrderAsc(Long shopId);

    // Lấy children trực tiếp của một category của shop
    List<Category> findByShopIdAndParentIdAndIsActiveTrueOrderBySortOrderAsc(Long shopId, Long parentId);

    // Kiểm tra tên trùng trong cùng parent của shop (null-safe)
    @Query("""
            SELECT COUNT(c) > 0 FROM Category c
            WHERE c.shop.id = :shopId
            AND c.name = :name
            AND c.isActive = true
            AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.id = :parentId)
            """)
    boolean existsByShopIdAndNameAndParentId(@Param("shopId") Long shopId, @Param("name") String name, @Param("parentId") Long parentId);

    // Kiểm tra tên trùng khi update của shop (loại trừ chính nó)
    @Query("""
            SELECT COUNT(c) > 0 FROM Category c
            WHERE c.shop.id = :shopId
            AND c.name = :name
            AND c.id <> :excludeId
            AND c.isActive = true
            AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.id = :parentId)
            """)
    boolean existsByShopIdAndNameAndParentIdExcludeId(
            @Param("shopId") Long shopId,
            @Param("name") String name,
            @Param("parentId") Long parentId,
            @Param("excludeId") Long excludeId);

    // Đếm sản phẩm thuộc category (để chặn xóa)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    // Đếm danh mục con của shop (để chặn xóa)
    long countByShopIdAndParentIdAndIsActiveTrue(Long shopId, Long parentId);

    // Load toàn bộ cây 1 lần của shop — build tree in-memory, tránh N+1
    @Query("SELECT c FROM Category c WHERE c.shop.id = :shopId AND c.isActive = true ORDER BY c.level ASC, c.sortOrder ASC")
    List<Category> findAllActiveOrderedByLevelAndSort(@Param("shopId") Long shopId);

    // Kiểm tra slug trùng trong shop
    boolean existsByShopIdAndSlugAndIdNot(Long shopId, String slug, Long id);
    boolean existsByShopIdAndSlug(Long shopId, String slug);

    /**
     * Lấy ID của chính danh mục đó VÀ tất cả danh mục con cấp 1.
     * Dùng cho bộ lọc sản phẩm: khi filter theo "Điện tử" (id=1),
     * sẽ trả về [1, 9, 10, 11, 12, 13] → lọc sản phẩm bằng IN.
     *
     * Hỗ trợ 2 cấp hiện tại (parent → children).
     * Nếu cần đệ quy nhiều cấp hơn → cần dùng CTE hoặc xử lý in-memory.
     */
    @Query("""
            SELECT c.id FROM Category c
            WHERE c.isActive = true
            AND (c.id = :categoryId OR c.parent.id = :categoryId)
            """)
    List<Long> findCategoryAndChildIds(@Param("categoryId") Long categoryId);
}
