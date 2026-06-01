package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Lấy tất cả root categories (không có parent)
    List<Category> findByParentIsNullAndIsActiveTrueOrderBySortOrderAsc();

    // Lấy children trực tiếp của một category
    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);

    // Kiểm tra tên trùng trong cùng parent (null-safe)
    @Query("""
            SELECT COUNT(c) > 0 FROM Category c
            WHERE c.name = :name
            AND c.isActive = true
            AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.id = :parentId)
            """)
    boolean existsByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    // Kiểm tra tên trùng khi update (loại trừ chính nó)
    @Query("""
            SELECT COUNT(c) > 0 FROM Category c
            WHERE c.name = :name
            AND c.id <> :excludeId
            AND c.isActive = true
            AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.id = :parentId)
            """)
    boolean existsByNameAndParentIdExcludeId(
            @Param("name") String name,
            @Param("parentId") Long parentId,
            @Param("excludeId") Long excludeId);

    // Đếm sản phẩm thuộc category (để chặn xóa)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    // Đếm danh mục con (để chặn xóa)
    long countByParentIdAndIsActiveTrue(Long parentId);

    // Load toàn bộ cây 1 lần — build tree in-memory, tránh N+1
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.level ASC, c.sortOrder ASC")
    List<Category> findAllActiveOrderedByLevelAndSort();

    // Kiểm tra slug trùng
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsBySlug(String slug);

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
