package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.CategoryRequest;
import com.huusang.demo.Dto.Response.CategoryResponse;
import com.huusang.demo.Entity.Category;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryService {

    CategoryRepository categoryRepository;

    // ─── GET TREE ────────────────────────────────────────────────────────────
    // Load toàn bộ 1 query, build tree in-memory → tránh N+1
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        try {
            log.info("🌳 Building category tree...");
            List<Category> all = categoryRepository.findAllActiveOrderedByLevelAndSort();
            log.info("📊 Found {} categories in database", all.size());

            Map<Long, CategoryResponse> nodeMap = new LinkedHashMap<>();
            List<CategoryResponse> roots = new ArrayList<>();

            // Pass 1: tạo tất cả node
            for (Category c : all) {
                CategoryResponse dto = toResponse(c);
                dto.setChildren(new ArrayList<>());
                nodeMap.put(c.getId(), dto);
                log.debug("📄 Created node for category: {} (ID: {}, Level: {})", c.getName(), c.getId(), c.getLevel());
            }

            // Pass 2: gắn children vào đúng parent
            for (Category c : all) {
                CategoryResponse dto = nodeMap.get(c.getId());
                if (c.getParent() == null) {
                    roots.add(dto);
                    log.debug("🌟 Root category: {} (ID: {})", c.getName(), c.getId());
                } else {
                    Long parentId = c.getParent().getId();
                    CategoryResponse parentDto = nodeMap.get(parentId);
                    if (parentDto != null) {
                        parentDto.getChildren().add(dto);
                        log.debug("🔗 Added {} (ID: {}) as child of parent ID: {}", c.getName(), c.getId(), parentId);
                    } else {
                        log.warn("⚠️ Parent not found in nodeMap for category: {} (ID: {}, Parent ID: {})", 
                            c.getName(), c.getId(), parentId);
                    }
                }
            }
            
            log.info("✅ Category tree built successfully. {} root categories, {} total categories", roots.size(), nodeMap.size());
            return roots;
        } catch (Exception e) {
            log.error("❌ Error building category tree: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    // ─── GET BY ID ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findOrThrow(id);
        CategoryResponse response = toResponse(category);

        // Lấy children trực tiếp (1 level)
        List<CategoryResponse> children = categoryRepository
                .findByParentIdAndIsActiveTrueOrderBySortOrderAsc(id)
                .stream()
                .map(this::toResponse)
                .toList();
        response.setChildren(children);
        return response;
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Validate tên trùng trong cùng parent
        if (categoryRepository.existsByNameAndParentId(request.getName(), request.getParentId())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(generateUniqueSlug(request.getName(), null))
                .description(request.getDescription() != null ? request.getDescription() : "")
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(true)
                .build();

        if (request.getParentId() != null) {
            Category parent = findOrThrow(request.getParentId());
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(0);
        }

        return toResponse(categoryRepository.save(category));
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);

        Long parentId = category.getParent() != null ? category.getParent().getId() : null;

        // Validate tên trùng (loại trừ chính nó)
        if (categoryRepository.existsByNameAndParentIdExcludeId(request.getName(), parentId, id)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription() != null ? request.getDescription() : "");
        category.setImageUrl(request.getImageUrl());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        return toResponse(categoryRepository.save(category));
    }

    // ─── DELETE (soft delete) ────────────────────────────────────────────────
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findOrThrow(id);

        // Chặn xóa nếu có danh mục con
        long childCount = categoryRepository.countByParentIdAndIsActiveTrue(id);
        if (childCount > 0) {
            throw new AppException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }

        // Chặn xóa nếu có sản phẩm
        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new AppException(ErrorCode.CATEGORY_HAS_PRODUCTS);
        }

        // Soft delete — không xóa vật lý
        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Category {} soft-deleted", id);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────
    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // ─── Helper: Count all categories ────────────────────────────────────────
    public long countAllCategories() {
        return categoryRepository.count();
    }

    // ─── Helper: Get all categories flat (no tree) ───────────────────────────
    public List<CategoryResponse> getAllCategoriesFlat() {
        try {
            List<Category> all = categoryRepository.findAllActiveOrderedByLevelAndSort();
            log.info("📊 Found {} active categories", all.size());
            return all.stream()
                    .map(this::toResponse)
                    .toList();
        } catch (Exception e) {
            log.error("❌ Error fetching categories: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .level(c.getLevel())
                .sortOrder(c.getSortOrder())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    // Tạo slug duy nhất từ tên tiếng Việt
    private String generateUniqueSlug(String name, Long excludeId) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        String slug = base;
        int counter = 1;

        // Đảm bảo slug unique
        while (excludeId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, excludeId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
