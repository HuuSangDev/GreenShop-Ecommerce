package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Tìm danh mục cha (parent = null)
    List<Category> findByParentIsNull();
    
    // Tìm danh mục con theo parent
    List<Category> findByParentId(Long parentId);
}
