package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.AddToCartRequest;
import com.huusang.demo.Dto.Request.UpdateCartItemRequest;
import com.huusang.demo.Dto.Response.CartItemResponse;
import com.huusang.demo.Dto.Response.CartResponse;
import com.huusang.demo.Dto.Response.CartValidationResponse;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartService {

    CartRepository            cartRepository;
    CartItemRepository        cartItemRepository;
    ProductRepository         productRepository;
    ProductVariantRepository  variantRepository;
    UserRepository            userRepository;

    // ─── GET CART ────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartIdWithDetails(cart.getId());
        return buildCartResponse(cart, items);
    }

    // ─── ADD TO CART ─────────────────────────────────────────────────────────
    @Transactional
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        // 1. Validate product tồn tại và đang active
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isAvailable()) {
            throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);
        }

        // 2. Validate variant thuộc đúng product
        variantRepository.findByIdAndProductId(request.getVariantId(), request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_BELONG_TO_PRODUCT));

        // 3. Đọc variant với optimistic lock để tránh race condition
        ProductVariant variant = variantRepository
                .findByIdWithOptimisticLock(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (variant.getStockQuantity() == null || variant.getStockQuantity() <= 0) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        Cart cart = getOrCreateCart(userId);

        // 4. Tìm item đã tồn tại trong giỏ (upsert logic)
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), variant.getId());

        int newQuantity = request.getQuantity();
        if (existingItem.isPresent()) {
            newQuantity += existingItem.get().getQuantity();
        }

        // 5. Validate tổng quantity không vượt stock
        if (newQuantity > variant.getStockQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // 6. Upsert
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            log.info("Updated cart item quantity: cartId={}, variantId={}, qty={}",
                    cart.getId(), variant.getId(), newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .priceSnapshot(variant.getPrice()) // Snapshot giá tại thời điểm thêm
                    .build();
            cartItemRepository.save(newItem);
            log.info("Added new cart item: cartId={}, variantId={}, qty={}",
                    cart.getId(), variant.getId(), request.getQuantity());
        }

        return getCart(userId);
    }

    // ─── UPDATE QUANTITY ─────────────────────────────────────────────────────
    @Transactional
    public CartResponse updateQuantity(String userId, String cartItemId,
                                       UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findCartItemBelongingToUser(cartItemId, cart.getId());

        // Check stock với optimistic lock
        ProductVariant variant = variantRepository
                .findByIdWithOptimisticLock(item.getProductVariant().getId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (variant.getStockQuantity() == null || variant.getStockQuantity() <= 0) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        if (request.getQuantity() > variant.getStockQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return getCart(userId);
    }

    // ─── REMOVE ITEM ─────────────────────────────────────────────────────────
    @Transactional
    public CartResponse removeItem(String userId, String cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findCartItemBelongingToUser(cartItemId, cart.getId());
        cartItemRepository.delete(item);
        return getCart(userId);
    }

    // ─── CLEAR CART ──────────────────────────────────────────────────────────
    @Transactional
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
        log.info("Cart cleared for userId={}", userId);
    }

    // ─── VALIDATE CART ───────────────────────────────────────────────────────
    // Gọi trước khi checkout — kiểm tra giá thay đổi và tồn kho
    @Transactional(readOnly = true)
    public CartValidationResponse validateCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartIdWithDetails(cart.getId());
        List<CartValidationResponse.CartItemWarning> warnings = new ArrayList<>();

        for (CartItem item : items) {
            ProductVariant variant = item.getProductVariant();
            Product product = item.getProduct();

            // Check sản phẩm còn active không
            if (!product.isAvailable()) {
                warnings.add(CartValidationResponse.CartItemWarning.builder()
                        .cartItemId(item.getId())
                        .productName(product.getProductName())
                        .variantName(variant.getVariantName())
                        .warningType("PRODUCT_UNAVAILABLE")
                        .message("Product is no longer available")
                        .build());
                continue; // Bỏ qua các check khác cho item này
            }

            // Check giá thay đổi
            if (variant.getPrice() != null &&
                    variant.getPrice().compareTo(item.getPriceSnapshot()) != 0) {
                warnings.add(CartValidationResponse.CartItemWarning.builder()
                        .cartItemId(item.getId())
                        .productName(product.getProductName())
                        .variantName(variant.getVariantName())
                        .warningType("PRICE_CHANGED")
                        .message("Price has changed since you added this item")
                        .oldPrice(item.getPriceSnapshot())
                        .newPrice(variant.getPrice())
                        .build());
            }

            // Check hết hàng hoàn toàn
            if (variant.getStockQuantity() == null || variant.getStockQuantity() <= 0) {
                warnings.add(CartValidationResponse.CartItemWarning.builder()
                        .cartItemId(item.getId())
                        .productName(product.getProductName())
                        .variantName(variant.getVariantName())
                        .warningType("OUT_OF_STOCK")
                        .message("This item is out of stock")
                        .requestedQty(item.getQuantity())
                        .availableQty(0)
                        .build());
            } else if (item.getQuantity() > variant.getStockQuantity()) {
                // Check không đủ số lượng
                warnings.add(CartValidationResponse.CartItemWarning.builder()
                        .cartItemId(item.getId())
                        .productName(product.getProductName())
                        .variantName(variant.getVariantName())
                        .warningType("INSUFFICIENT_STOCK")
                        .message("Requested quantity exceeds available stock")
                        .requestedQty(item.getQuantity())
                        .availableQty(variant.getStockQuantity())
                        .build());
            }
        }

        return CartValidationResponse.builder()
                .isValid(warnings.isEmpty())
                .warnings(warnings)
                .build();
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    // Lấy cart của user, tự tạo mới nếu chưa có
    // Nhận email (JWT subject) → resolve thành User entity
    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    // Tìm cart item và đảm bảo nó thuộc cart của user đang request
    private CartItem findCartItemBelongingToUser(String cartItemId, String cartId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!item.getCart().getId().equals(cartId)) {
            // Trả về NOT_FOUND thay vì FORBIDDEN để không lộ thông tin
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return item;
    }

    // Build CartResponse từ danh sách CartItem
    private CartResponse buildCartResponse(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(item -> {
            ProductVariant variant = item.getProductVariant();
            Product product = item.getProduct();
            boolean priceChanged = variant.getPrice() != null &&
                    variant.getPrice().compareTo(item.getPriceSnapshot()) != 0;

            BigDecimal subtotal = item.getPriceSnapshot()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            return CartItemResponse.builder()
                    .cartItemId(item.getId())
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .productImageUrl(product.getImageUrl())
                    .variantId(variant.getId())
                    .variantName(variant.getVariantName())
                    .sku(variant.getSku())
                    .quantity(item.getQuantity())
                    .priceSnapshot(item.getPriceSnapshot())
                    .currentPrice(variant.getPrice())
                    .priceChanged(priceChanged)
                    .availableStock(variant.getStockQuantity())
                    .inStock(variant.getStockQuantity() != null && variant.getStockQuantity() > 0)
                    .subtotal(subtotal)
                    .addedAt(item.getAddedAt())
                    .build();
        }).toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .totalDistinctItems(itemResponses.size())
                .build();
    }
}
