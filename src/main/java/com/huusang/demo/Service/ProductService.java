package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.*;
import com.huusang.demo.Dto.Response.ProductResponse;
import com.huusang.demo.Dto.Response.ProductVariantResponse;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Exception.BadRequestException;
import com.huusang.demo.Exception.ResourceNotFoundException;
import com.huusang.demo.Exception.UnauthorizedException;
import com.huusang.demo.Mapper.ProductMapper;
import com.huusang.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, String email) {
        // 1. Tìm User bằng email từ token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        // 2. Lấy ID của User để tìm Shop
        Shop shop = shopRepository.findByOwnerId(user.getId()) // Truyền ID vào đây
                .orElseThrow(() -> new BadRequestException("Bạn chưa có shop. Vui lòng tạo shop trước"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        // Lấy file ảnh từ bên trong DTO, lưu vào disk nếu có
        String imageUrl = null;
        MultipartFile image = request.getImage();
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.storeFile(image, "products");
        }

        Product product = Product.builder()
                .shop(shop)
                .category(category)
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(imageUrl)   // relative path: "products/uuid.jpg"
                .available(true)
                .build();

        product = productRepository.save(product);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variants = new ArrayList<>();
            for (ProductVariantRequest variantReq : request.getVariants()) {
                variants.add(ProductVariant.builder()
                        .product(product)
                        .variantName(variantReq.getVariantName())
                        .price(variantReq.getPrice())
                        .stockQuantity(variantReq.getStockQuantity())
                        .sku(variantReq.getSku())
                        .build());
            }
            variants = variantRepository.saveAll(variants);
            product.setVariants(variants);
        }

        return productMapper.toResponse(product);
    }

    /**
     * Cập nhật thông tin sản phẩm.
     * image được lấy từ request.getImage() — null = giữ nguyên ảnh cũ.
     */
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request, String email) {
        // Tìm User bằng email từ JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        if (!product.getShop().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền sửa sản phẩm này");
        }

        if (request.getProductName() != null)   product.setProductName(request.getProductName());
        if (request.getDescription() != null)   product.setDescription(request.getDescription());
        if (request.getPrice() != null)         product.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }

        // Lấy file ảnh từ bên trong DTO
        MultipartFile image = request.getImage();
        if (image != null && !image.isEmpty()) {
            fileStorageService.deleteFile(product.getImageUrl()); // xóa ảnh cũ
            product.setImageUrl(fileStorageService.storeFile(image, "products"));
        }

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    /**
     * Soft delete sản phẩm (ẩn sản phẩm)
     * Chỉ chủ shop mới được xóa
     */
    @Transactional
    public void deleteProduct(Long productId, String email) {
        // Tìm User bằng email từ JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        // Kiểm tra quyền sở hữu
        if (!product.getShop().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền xóa sản phẩm này");
        }

        // Soft delete
        product.setAvailable(false);
        productRepository.save(product);
    }

    /**
     * Lấy chi tiết sản phẩm kèm variants
     */
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findByIdAndAvailableTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return productMapper.toResponse(product);
    }

    /**
     * Lấy số lượng đã bán của sản phẩm
     */
    public Integer getSoldCount(Long productId) {
        Product product = productRepository.findByIdAndAvailableTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return product.getSoldCount() != null ? product.getSoldCount() : 0;
    }

    /**
     * Lấy danh sách sản phẩm của 1 shop (có phân trang)
     */
    public Page<ProductResponse> getProductsByShop(Long shopId, String status, int page, int size) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shop"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        String filterStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toLowerCase() : "all";
        Page<Product> products = productRepository.findProductsByShopAndStatus(shopId, filterStatus, pageable);

        return products.map(productMapper::toResponse);
    }

    /**
     * Thêm variant mới vào sản phẩm
     */
    @Transactional
    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request, String email) {
        // Tìm User bằng email từ JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        // Kiểm tra quyền sở hữu
        if (!product.getShop().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền thêm variant cho sản phẩm này");
        }

        // Kiểm tra SKU trùng
        if (variantRepository.findBySku(request.getSku()).isPresent()) {
            throw new BadRequestException("SKU đã tồn tại");
        }

        // Upload ảnh biến thể nếu có
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = fileStorageService.storeFile(request.getImage(), "products/variants");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .variantName(request.getVariantName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(request.getSku())
                .imageUrl(imageUrl)
                .build();

        variant = variantRepository.save(variant);
        return productMapper.toVariantResponse(variant);
    }

    /**
     * Cập nhật variant
     */
    @Transactional
    public ProductVariantResponse updateVariant(Long variantId, ProductVariantUpdateRequest request, String email) {
        // Tìm User bằng email từ JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy variant"));

        // Kiểm tra quyền sở hữu
        if (!variant.getProduct().getShop().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền sửa variant này");
        }

        // Cập nhật các trường
        if (request.getVariantName() != null) {
            variant.setVariantName(request.getVariantName());
        }
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            variant.setStockQuantity(request.getStockQuantity());
        }
        if (request.getSku() != null) {
            // Kiểm tra SKU trùng
            variantRepository.findBySku(request.getSku()).ifPresent(existing -> {
                if (!existing.getId().equals(variantId)) {
                    throw new BadRequestException("SKU đã tồn tại");
                }
            });
            variant.setSku(request.getSku());
        }

        // Cập nhật ảnh nếu có
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (variant.getImageUrl() != null) {
                fileStorageService.deleteFile(variant.getImageUrl());
            }
            variant.setImageUrl(fileStorageService.storeFile(request.getImage(), "products/variants"));
        }

        variant = variantRepository.save(variant);
        return productMapper.toVariantResponse(variant);
    }

    /**
     * Xóa variant (chặn nếu đang trong đơn hàng active)
     */
    @Transactional
    public void deleteVariant(Long variantId, String email) {
        // Tìm User bằng email từ JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy variant"));

        // Kiểm tra quyền sở hữu
        if (!variant.getProduct().getShop().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền xóa variant này");
        }

        // Kiểm tra variant có trong đơn hàng active không
        if (variantRepository.isVariantInActiveOrder(variant.getId())) {
            throw new BadRequestException("Không thể xóa variant đang có trong đơn hàng chưa hoàn thành");
        }

        variantRepository.delete(variant);
    }

    /**
     * Cập nhật tồn kho thủ công
     */
    @Transactional
    public ProductVariantResponse updateStock(Long variantId, Integer quantity, String email) {
        // Tìm User bằng email từ JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy variant"));

        // Kiểm tra quyền sở hữu
        if (!variant.getProduct().getShop().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật tồn kho");
        }

        if (quantity < 0) {
            throw new BadRequestException("Số lượng tồn kho không được âm");
        }

        variant.setStockQuantity(quantity);
        variant = variantRepository.save(variant);
        return productMapper.toVariantResponse(variant);
    }

    /**
     * Tìm kiếm sản phẩm theo keyword
     */
    public Page<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.searchProducts(keyword, pageable);
        return products.map(productMapper::toResponse);
    }

    /**
     * Lọc sản phẩm theo nhiều tiêu chí.
     *
     * FIX: Nếu có categoryId, lấy thêm tất cả ID của danh mục con
     * → dùng IN (:categoryIds) thay vì = :categoryId
     * → Lọc theo danh mục cha sẽ ra cả sản phẩm của danh mục con.
     */
    public Page<ProductResponse> filterProducts(ProductFilterRequest request) {
        Sort sort = request.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Resolve categoryIds: bao gồm danh mục cha + tất cả danh mục con cấp 1
        List<Long> categoryIds = null;
        if (request.getCategoryId() != null) {
            categoryIds = categoryRepository.findCategoryAndChildIds(request.getCategoryId());
            // Đảm bảo luôn có ít nhất ID của chính danh mục được truyền vào
            // (phòng trường hợp danh mục bị inactive nhưng request vẫn truyền ID)
            if (categoryIds.isEmpty()) {
                categoryIds = List.of(request.getCategoryId());
            }
        }

        Page<Product> products = productRepository.filterProductsByCategories(
                categoryIds,
                request.getShopId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getKeyword(),
                pageable
        );

        return products.map(productMapper::toResponse);
    }

    /**
     * Sản phẩm bán chạy nhất
     */
    public Page<ProductResponse> getTopSellingProducts(int page, int size, Long shopId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products;

        if (shopId != null) {
            products = productRepository.findTopSellingProductsByShop(shopId, pageable);
        } else {
            products = productRepository.findTopSellingProducts(pageable);
        }

        return products.map(productMapper::toResponse);
    }

    /**
     * Sản phẩm mới nhất
     */
    public Page<ProductResponse> getNewArrivals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByAvailableTrueOrderByCreatedAtDesc(pageable);
        return products.map(productMapper::toResponse);
    }

    /**
     * Admin ẩn sản phẩm vi phạm
     */
    @Transactional
    public void adminHideProduct(Long productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        product.setHidden(true);
        product.setHideReason(reason != null ? reason : "");
        productRepository.save(product);

        if (product.getShop() != null && product.getShop().getOwner() != null) {
            notificationService.sendNotification(
                    product.getShop().getOwner(),
                    com.huusang.demo.Enum.NotificationType.PRODUCT_HIDDEN,
                    "Sản phẩm bị ẩn",
                    "Sản phẩm " + product.getProductName() + " của bạn đã bị admin ẩn vì lý do: " + (reason != null ? reason : "Không có lý do cụ thể"),
                    product.getId().toString()
            );
        }
    }

    @Transactional
    public void adminUnhideProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        product.setHidden(false);
        product.setHideReason(null);
        productRepository.save(product);
    }

    /**
     * Admin lấy tất cả sản phẩm (kể cả ẩn)
     */
    public Page<ProductResponse> getAllProductsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findByHiddenFalse(pageable);
        return products.map(productMapper::toResponse);
    }

    /**
     * Admin lấy sản phẩm theo trạng thái hidden
     * hidden = true: sản phẩm đã ẩn
     * hidden = false: sản phẩm không bị ẩn
     */
    public Page<ProductResponse> getProductsByHiddenStatus(boolean hidden, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findByHidden(hidden, pageable);
        return products.map(productMapper::toResponse);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UPLOAD ẢNH
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Upload ảnh cho sản phẩm.
     * Lưu file vào thư mục uploads/products/, cập nhật imageUrl trong DB.
     *
     * @param productId ID sản phẩm
     * @param file      file ảnh từ request
     * @param userId    email của seller (kiểm tra quyền sở hữu)
     * @return ProductResponse với imageUrl mới
     */
    @Transactional
    public ProductResponse uploadProductImage(Long productId, MultipartFile file, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        // Kiểm tra quyền sở hữu
        if (!product.getShop().getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật ảnh sản phẩm này");
        }

        // Xóa ảnh cũ nếu đã có
        fileStorageService.deleteFile(product.getImageUrl());

        // Lưu ảnh mới vào uploads/products/
        String imageUrl = fileStorageService.storeFile(file, "products");
        product.setImageUrl(imageUrl);
        productRepository.save(product);

        return productMapper.toResponse(product);
    }
}
