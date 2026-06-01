package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.CategoryRequest;
import com.huusang.demo.Dto.Response.CategoryResponse;
import com.huusang.demo.Entity.Category;
import com.huusang.demo.Entity.Shop;
import com.huusang.demo.Entity.User;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.CategoryRepository;
import com.huusang.demo.Repository.ShopRepository;
import com.huusang.demo.Repository.UserRepository;
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
    ShopRepository shopRepository;
    UserRepository userRepository;

    // ─── HELPER TO GET SHOP BY USER EMAIL ────────────────────────────────────
    private Shop getShopByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }

    // ─── GET TREE (SHOP-SPECIFIC) ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree(String email, Long shopId) {
        Long targetShopId = shopId;
        if (targetShopId == null && email != null) {
            try {
                Shop shop = getShopByEmail(email);
                targetShopId = shop.getId();
            } catch (Exception e) {
                log.warn("Could not resolve shop for email {}: {}", email, e.getMessage());
            }
        }

        if (targetShopId == null) {
            return Collections.emptyList();
        }

        List<Category> all = categoryRepository.findAllActiveOrderedByLevelAndSort(targetShopId);

        Map<Long, CategoryResponse> nodeMap = new LinkedHashMap<>();
        List<CategoryResponse> roots = new ArrayList<>();

        // Pass 1: tạo tất cả node
        for (Category c : all) {
            CategoryResponse dto = toResponse(c);
            dto.setChildren(new ArrayList<>());
            nodeMap.put(c.getId(), dto);
        }

        // Pass 2: gắn children vào đúng parent
        for (Category c : all) {
            CategoryResponse dto = nodeMap.get(c.getId());
            if (c.getParent() == null) {
                roots.add(dto);
            } else {
                CategoryResponse parentDto = nodeMap.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }
        return roots;
    }

    // ─── GET BY ID ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findOrThrow(id);
        CategoryResponse response = toResponse(category);

        // Lấy children trực tiếp (1 level)
        List<CategoryResponse> children = categoryRepository
                .findByShopIdAndParentIdAndIsActiveTrueOrderBySortOrderAsc(category.getShop().getId(), id)
                .stream()
                .map(this::toResponse)
                .toList();
        response.setChildren(children);
        return response;
    }

    // ─── CREATE (SHOP-SPECIFIC) ──────────────────────────────────────────────
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, String email) {
        Shop shop = getShopByEmail(email);

        // Validate tên trùng trong cùng parent của shop
        if (categoryRepository.existsByShopIdAndNameAndParentId(shop.getId(), request.getName(), request.getParentId())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
        }

        Category category = Category.builder()
                .shop(shop)
                .name(request.getName())
                .slug(generateUniqueSlug(shop.getId(), request.getName(), null))
                .description(request.getDescription() != null ? request.getDescription() : "")
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(true)
                .build();

        if (request.getParentId() != null) {
            Category parent = findOrThrow(request.getParentId());
            // Đảm bảo parent thuộc cùng một shop
            if (!parent.getShop().getId().equals(shop.getId())) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(0);
        }

        return toResponse(categoryRepository.save(category));
    }

    // ─── UPDATE (SHOP-SPECIFIC) ──────────────────────────────────────────────
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request, String email) {
        Shop shop = getShopByEmail(email);
        Category category = findOrThrow(id);

        // Đảm bảo danh mục thuộc về shop của người dùng hiện tại
        if (!category.getShop().getId().equals(shop.getId())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Long parentId = category.getParent() != null ? category.getParent().getId() : null;

        // Validate tên trùng (loại trừ chính nó) của shop
        if (categoryRepository.existsByShopIdAndNameAndParentIdExcludeId(shop.getId(), request.getName(), parentId, id)) {
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
    public void deleteCategory(Long id, String email) {
        Shop shop = getShopByEmail(email);
        Category category = findOrThrow(id);

        // Đảm bảo thuộc về shop của mình
        if (!category.getShop().getId().equals(shop.getId())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // Chặn xóa nếu có danh mục con
        long childCount = categoryRepository.countByShopIdAndParentIdAndIsActiveTrue(shop.getId(), id);
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
        log.info("Category {} soft-deleted for shop {}", id, shop.getId());
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────
    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .shopId(c.getShop() != null ? c.getShop().getId() : null)
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

    // Tạo slug duy nhất từ tên tiếng Việt cho một shop cụ thể
    private String generateUniqueSlug(Long shopId, String name, Long excludeId) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        String slug = base;
        int counter = 1;

        // Đảm bảo slug unique trong shop này
        while (excludeId == null
                ? categoryRepository.existsByShopIdAndSlug(shopId, slug)
                : categoryRepository.existsByShopIdAndSlugAndIdNot(shopId, slug, excludeId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
