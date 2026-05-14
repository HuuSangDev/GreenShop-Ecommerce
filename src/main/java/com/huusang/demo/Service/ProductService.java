package com.huusang.demo.Service;

import com.huusang.demo.DTO.Request.*;
import com.huusang.demo.DTO.Response.ProductResponse;
import com.huusang.demo.DTO.Response.ProductVariantResponse;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ==================== HELPERS ====================

    private String getCurrentUserId() {
        // JWT subject là email (xem AuthService.generateToken)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }

    private Shop getShopByUserId(String userId) {
        return shopRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_OWNED));
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void checkProductOwnership(Product product, String userId) {
        if (!product.getShop().getOwner().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_PRODUCT);
        }
    }

    private ProductVariantResponse toVariantResponse(ProductVariant v) {
        return ProductVariantResponse.builder()
                .id(v.getId())
                .variantName(v.getVariantName())
                .price(v.getPrice())
                .stockQuantity(v.getStockQuantity())
                .sku(v.getSku())
                .build();
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .imageUrl(p.getImageUrl())
                .available(p.isAvailable())
                .createdAt(p.getCreatedAt())
                .shopId(p.getShop() != null ? p.getShop().getId() : null)
                .shopName(p.getShop() != null ? p.getShop().getShopName() : null)
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : null)
                .variants(p.getVariants() != null
                        ? p.getVariants().stream().map(this::toVariantResponse).collect(Collectors.toList())
                        : null)
                .build();
    }

    // ==================== SELLER ====================

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        String userId = getCurrentUserId();
        Shop shop = getShopByUserId(userId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.builder()
                .shop(shop)
                .category(category)
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .available(true)
                .build();

        product = productRepository.save(product);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variants = new ArrayList<>();
            for (ProductVariantRequest vr : request.getVariants()) {
                if (variantRepository.existsBySku(vr.getSku())) {
                    throw new AppException(ErrorCode.SKU_EXISTED);
                }
                variants.add(ProductVariant.builder()
                        .product(product)
                        .variantName(vr.getVariantName())
                        .price(vr.getPrice())
                        .stockQuantity(vr.getStockQuantity())
                        .sku(vr.getSku())
                        .build());
            }
            product.setVariants(variantRepository.saveAll(variants));
        }

        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        String userId = getCurrentUserId();
        Product product = getProductOrThrow(productId);
        checkProductOwnership(product, userId);

        if (request.getProductName() != null)  product.setProductName(request.getProductName());
        if (request.getDescription() != null)  product.setDescription(request.getDescription());
        if (request.getPrice() != null)        product.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getImageUrl() != null)     product.setImageUrl(request.getImageUrl());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        String userId = getCurrentUserId();
        Product product = getProductOrThrow(productId);
        checkProductOwnership(product, userId);
        product.setAvailable(false);
        productRepository.save(product);
    }

    // ==================== PUBLIC ====================

    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findByIdAndAvailableTrue(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toResponse(product);
    }

    public Page<ProductResponse> getProductsByShop(Long shopId, int page, int size) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByShopAndAvailableTrue(shop, pageable).map(this::toResponse);
    }

    public Page<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.searchProducts(keyword, pageable).map(this::toResponse);
    }

    public Page<ProductResponse> filterProducts(ProductFilterRequest request) {
        Sort sort = request.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        return productRepository.filterProducts(
                request.getCategoryId(), request.getShopId(),
                request.getMinPrice(), request.getMaxPrice(), pageable
        ).map(this::toResponse);
    }

    public Page<ProductResponse> getTopSellingProducts(int page, int size, Long shopId) {
        Pageable pageable = PageRequest.of(page, size);
        if (shopId != null) {
            return productRepository.findTopSellingProductsByShop(shopId, pageable).map(this::toResponse);
        }
        return productRepository.findTopSellingProducts(pageable).map(this::toResponse);
    }

    public Page<ProductResponse> getNewArrivals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByAvailableTrueOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    // ==================== VARIANT ====================

    @Transactional
    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request) {
        String userId = getCurrentUserId();
        Product product = getProductOrThrow(productId);
        checkProductOwnership(product, userId);

        if (variantRepository.existsBySku(request.getSku())) {
            throw new AppException(ErrorCode.SKU_EXISTED);
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .variantName(request.getVariantName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(request.getSku())
                .build();

        return toVariantResponse(variantRepository.save(variant));
    }

    @Transactional
    public ProductVariantResponse updateVariant(Long variantId, ProductVariantUpdateRequest request) {
        String userId = getCurrentUserId();
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        checkProductOwnership(variant.getProduct(), userId);

        if (request.getVariantName() != null) variant.setVariantName(request.getVariantName());
        if (request.getPrice() != null)       variant.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) variant.setStockQuantity(request.getStockQuantity());
        if (request.getSku() != null) {
            variantRepository.findBySku(request.getSku()).ifPresent(existing -> {
                if (!existing.getId().equals(variantId)) throw new AppException(ErrorCode.SKU_EXISTED);
            });
            variant.setSku(request.getSku());
        }

        return toVariantResponse(variantRepository.save(variant));
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        String userId = getCurrentUserId();
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        checkProductOwnership(variant.getProduct(), userId);

        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING, OrderStatus.PREPARING,
                OrderStatus.READY_TO_SHIP, OrderStatus.SHIPPED
        );
        if (variantRepository.isVariantInActiveOrder(variantId, activeStatuses)) {
            throw new AppException(ErrorCode.VARIANT_IN_ACTIVE_ORDER);
        }

        variantRepository.delete(variant);
    }

    @Transactional
    public ProductVariantResponse updateStock(Long variantId, Integer quantity) {
        String userId = getCurrentUserId();
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
        checkProductOwnership(variant.getProduct(), userId);

        variant.setStockQuantity(quantity);
        return toVariantResponse(variantRepository.save(variant));
    }

    // ==================== ADMIN ====================

    @Transactional
    public void adminHideProduct(Long productId) {
        Product product = getProductOrThrow(productId);
        product.setAvailable(false);
        productRepository.save(product);
    }

    @Transactional
    public void adminUnhideProduct(Long productId) {
        Product product = getProductOrThrow(productId);
        product.setAvailable(true);
        productRepository.save(product);
    }

    public Page<ProductResponse> getAllProductsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<ProductResponse> getProductsPendingReview(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByAvailableFalse(pageable).map(this::toResponse);
    }
}
