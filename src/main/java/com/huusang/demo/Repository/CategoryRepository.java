package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
>>>>>>> 2fcffb4418523408e449bdcfc70909241c4c77c4
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
<<<<<<< HEAD

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
=======
    
    // Tìm danh mục cha (parent = null)
    List<Category> findByParentIsNull();
    
    // Tìm danh mục con theo parent
    List<Category> findByParentId(Long parentId);
>>>>>>> 2fcffb4418523408e449bdcfc70909241c4c77c4
}
