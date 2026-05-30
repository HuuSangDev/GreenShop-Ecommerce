package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.CheckoutPreviewRequest;
import com.huusang.demo.Dto.Request.CheckoutRequest;
import com.huusang.demo.Dto.Request.GhnFeeRequest;
import com.huusang.demo.Dto.Request.SePayWebhookRequest;
import com.huusang.demo.Dto.Response.CheckoutPreviewResponse;
import com.huusang.demo.Dto.Response.OrderItemResponse;
import com.huusang.demo.Dto.Response.OrderResponse;
import com.huusang.demo.Dto.Response.ShopOrderResponse;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Enum.PaymentMethod;
import com.huusang.demo.Enum.PaymentStatus;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Exception.OutOfStockException;
import com.huusang.demo.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

    // ─── Repositories ─────────────────────────────────────────────────────────
    CartItemRepository       cartItemRepository;
    ProductVariantRepository variantRepository;
    OrderRepository          orderRepository;
    OrderItemRepository      orderItemRepository;
    ShopOrderRepository      shopOrderRepository;
    PaymentRepository        paymentRepository;
    UserRepository           userRepository;
    VoucherRepository        voucherRepository;

    // ─── Services ─────────────────────────────────────────────────────────────
    SePayService     sePayService;
    ShippingService  shippingService;
    VoucherService   voucherService;

    /**
     * Cân nặng mặc định (gram) khi sản phẩm không khai báo weight.
     * Áp dụng cho mỗi item (nhân với quantity).
     */
    static final int DEFAULT_WEIGHT_PER_ITEM_GRAM = 200;

    // ═══════════════════════════════════════════════════════════════════════════
    // STEP 0 — CHECKOUT PREVIEW
    // POST /api/v1/checkouts/preview
    // Read-only — KHÔNG tạo order, KHÔNG trừ stock, KHÔNG tạo payment.
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Tính toán preview đơn hàng:
     *   - Tính phí ship THẬT từ GHN cho từng shop (nếu có toDistrictId/toWardCode)
     *   - Tính giảm giá THẬT từ voucher (nếu có voucherCode)
     *   - Trả về breakdown để frontend hiển thị trang xác nhận
     */
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse previewCheckout(String userEmail, CheckoutPreviewRequest request) {

        User buyer = resolveUser(userEmail);
        List<CartItem> cartItems = fetchAndValidateCartItems(request.getCartItemIds(), buyer);

        // ─── 1. Tính tiền hàng từng item ─────────────────────────────────────────
        List<CheckoutPreviewResponse.PreviewItemResponse> itemResponses = cartItems.stream()
                .map(item -> {
                    ProductVariant variant = item.getProductVariant();
                    BigDecimal price = variant.getPrice();
                    int qty = item.getQuantity();
                    return CheckoutPreviewResponse.PreviewItemResponse.builder()
                            .cartItemId(item.getId())
                            .variantId(variant.getId())
                            .variantName(variant.getVariantName())
                            .productName(item.getProduct().getProductName())
                            .imageUrl(item.getProduct().getImageUrl())
                            .quantity(qty)
                            .price(price)
                            .subtotal(price.multiply(BigDecimal.valueOf(qty)))
                            .availableStock(variant.getStockQuantity())
                            .build();
                })
                .toList();

        BigDecimal subtotal = itemResponses.stream()
                .map(CheckoutPreviewResponse.PreviewItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ─── 2. Tính phí ship THẬT: gọi GHN cho từng shop ────────────────────────
        BigDecimal shippingFee = BigDecimal.ZERO;
        if (request.getToDistrictId() != null && request.getToWardCode() != null) {
            Map<Shop, List<CartItem>> itemsByShop = groupItemsByShop(cartItems);
            shippingFee = calcTotalShippingFee(itemsByShop, request.getToDistrictId(), request.getToWardCode());
        } else {
            log.info("Preview: toDistrictId/toWardCode không có → shippingFee = 0");
        }

        // ─── 3. Tính giảm giá THẬT từ voucher ────────────────────────────────────
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            try {
                Voucher voucher = voucherService.validateVoucher(
                        request.getVoucherCode(), subtotal, buyer.getId()
                );
                discountAmount = voucherService.calculateDiscount(voucher, subtotal);
                log.info("Preview: voucher='{}' → discount={}", request.getVoucherCode(), discountAmount);
            } catch (AppException e) {
                // Voucher không hợp lệ ở preview → log warn, không throw (UI tự hiện thông báo)
                log.warn("Preview: voucher '{}' không hợp lệ: {}", request.getVoucherCode(), e.getMessage());
            }
        }

        // ─── 4. Tính finalAmount ──────────────────────────────────────────────────
        BigDecimal finalAmount = subtotal.add(shippingFee).subtract(discountAmount).max(BigDecimal.ZERO);
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();

        return CheckoutPreviewResponse.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .totalItems(totalItems)
                .totalDistinctItems(cartItems.size())
                .items(itemResponses)
                .availablePaymentMethods(List.of(PaymentMethod.COD, PaymentMethod.SEPAY))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STEP 1 — CHECKOUT (COD hoặc SEPAY)
    // POST /api/v1/orders
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Đặt hàng Multi-Vendor — phân nhánh theo paymentMethod.
     * <p>
     * COD  → deduct stock ngay, order = PENDING
     * SEPAY → KHÔNG deduct stock, order = PENDING_PAYMENT, tạo Payment + QR URL
     * <p>
     * finalAmount = totalAmount (tiền hàng) + shippingFee - discountAmount
     */
    @Transactional
    public OrderResponse processMultiVendorCheckout(String userEmail, CheckoutRequest request) {

        // ─── 0. Resolve user ─────────────────────────────────────────────────────
        User buyer = resolveUser(userEmail);

        // ─── 1. Lấy giỏ hàng & validate ownership ────────────────────────────────
        List<CartItem> cartItems = fetchAndValidateCartItems(request.getCartItemIds(), buyer);

        // ─── 2. Kiểm tra tồn kho ─────────────────────────────────────────────────
        PaymentMethod paymentMethod = request.getPaymentMethod();
        if (paymentMethod == PaymentMethod.COD) {
            lockAndValidateStock(cartItems);
        } else if (paymentMethod == PaymentMethod.SEPAY) {
            validateStockOnly(cartItems);
        } else {
            throw new AppException(ErrorCode.ORDER_INVALID_PAYMENT_METHOD);
        }

        // ─── 3. Tính tiền hàng — backend tự tính, không trust frontend ───────────
        BigDecimal totalAmount = calcTotalAmount(cartItems);

        // ─── 4. Tính phí ship THẬT cho từng shop ─────────────────────────────────
        Map<Shop, List<CartItem>> itemsByShop = groupItemsByShop(cartItems);
        Map<Shop, BigDecimal> shippingFeeByShop = calcShippingFeeByShop(
                itemsByShop, request.getToDistrictId(), request.getToWardCode()
        );
        BigDecimal totalShippingFee = shippingFeeByShop.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ─── 5. Tính giảm giá từ voucher ─────────────────────────────────────────
        Voucher appliedVoucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            appliedVoucher = voucherService.validateVoucher(
                    request.getVoucherCode(), totalAmount, buyer.getId()
            );
            discountAmount = voucherService.calculateDiscount(appliedVoucher, totalAmount);
            log.info("Checkout: voucher='{}' applied, discount={}", request.getVoucherCode(), discountAmount);
        }

        // finalAmount = tiền hàng + ship - giảm giá (tối thiểu 0)
        BigDecimal finalAmount = totalAmount.add(totalShippingFee).subtract(discountAmount).max(BigDecimal.ZERO);

        // ─── 6. Tạo Order ─────────────────────────────────────────────────────────
        OrderStatus initialStatus = (paymentMethod == PaymentMethod.COD)
                ? OrderStatus.PENDING
                : OrderStatus.PENDING_PAYMENT;

        Order savedOrder = orderRepository.save(Order.builder()
                .buyer(buyer)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(initialStatus)
                .paymentMethod(paymentMethod.name())
                .build());

        log.info("Order created: orderId={}, buyer={}, method={}, goods={}, ship={}, discount={}, final={}",
                savedOrder.getId(), buyer.getId(), paymentMethod,
                totalAmount, totalShippingFee, discountAmount, finalAmount);

        // ─── 7. Tạo ShopOrder + OrderItem cho từng shop (kèm shippingFee thật) ───
        List<ShopOrderResponse> shopOrderResponses = buildShopOrders(
                savedOrder, itemsByShop, shippingFeeByShop, paymentMethod
        );

        // ─── 8. Đánh dấu voucher đã dùng (sau khi order commit) ──────────────────
        if (appliedVoucher != null) {
            voucherService.markVoucherUsed(appliedVoucher.getId(), buyer.getId(), savedOrder);
        }

        // ─── 9. Xử lý theo paymentMethod ─────────────────────────────────────────
        String paymentUrl = null;

        if (paymentMethod == PaymentMethod.COD) {
            cartItemRepository.deleteAllInBatch(cartItems);
            log.info("COD checkout done: orderId={}, cartItemsDeleted={}", savedOrder.getId(), cartItems.size());

        } else {
            // SEPAY: QR URL dùng finalAmount (tiền hàng + ship - discount)
            String transactionRef = "ORDER" + savedOrder.getId();
            paymentUrl = sePayService.generateQrUrl(finalAmount, transactionRef);

            paymentRepository.save(Payment.builder()
                    .order(savedOrder)
                    .method(PaymentMethod.SEPAY)
                    .status(PaymentStatus.PENDING)
                    .amount(finalAmount)           // ← finalAmount, không phải totalAmount
                    .transactionRef(transactionRef)
                    .checkoutUrl(paymentUrl)
                    .build());

            log.info("SEPAY checkout created: orderId={}, transactionRef={}, amount={}",
                    savedOrder.getId(), transactionRef, finalAmount);
        }

        // ─── 10. Build response ───────────────────────────────────────────────────
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .status(initialStatus)
                .paymentMethod(paymentMethod)
                .totalAmount(totalAmount)
                .shippingFee(totalShippingFee)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .createdAt(savedOrder.getCreatedAt())
                .totalShops(shopOrderResponses.size())
                .totalItems(totalItems)
                .shopOrders(shopOrderResponses)
                .paymentUrl(paymentUrl)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GET ORDER BY ID
    // GET /api/v1/orders/{orderId}
    // Dùng cho frontend polling trạng thái sau khi đặt hàng SEPAY.
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lấy trạng thái đơn hàng theo ID.
     * Dùng cho frontend polling: PENDING_PAYMENT → PAID → navigate sang success.
     * Chỉ trả orderId + status — thông tin đơn hàng đã có từ lúc checkout.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String userEmail, Long orderId) {
        User buyer = resolveUser(userEmail);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Chỉ cho xem đơn của chính mình
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STEP 2 — SEPAY WEBHOOK CALLBACK
    // POST /api/v1/payments/sepay/webhook  (public, không cần JWT)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý callback từ SePay khi giao dịch thành công.
     * Flow: parse transactionRef → verify amount → lock+deduct stock → update Payment+Order → clear cart
     */
    @Transactional
    public void handleSePayCallback(SePayWebhookRequest webhookRequest) {
        String content = webhookRequest.getContent();
        log.info("SePay webhook received: content='{}', amount={}", content, webhookRequest.getTransferAmount());

        // 1. Tìm Payment (flexible match: "ORDER4", "ORDER_4", "...ORDER4...")
        Payment payment = findPaymentByContent(content);

        // 2. Idempotency check
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("SePay webhook: already processed, transactionRef='{}'", content);
            return;
        }

        Order order = payment.getOrder();

        // 3. Verify amount (webhook phải >= amount lưu trong Payment)
        if (webhookRequest.getTransferAmount().compareTo(payment.getAmount()) < 0) {
            log.error("SePay webhook: amount mismatch. Expected={}, received={}",
                    payment.getAmount(), webhookRequest.getTransferAmount());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return;
        }

        // 4. Lock + Deduct stock theo thứ tự ID tăng dần (tránh deadlock)
        List<OrderItem> orderItems = orderItemRepository.findByShopOrderOrderId(order.getId());
        orderItems.stream()
                .sorted(Comparator.comparingLong(oi -> oi.getProductVariant().getId()))
                .forEach(orderItem -> {
                    Long variantId = orderItem.getProductVariant().getId();
                    int qty = orderItem.getQuantity();

                    ProductVariant locked = variantRepository.findByIdForUpdate(variantId)
                            .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                    if (locked.getStockQuantity() == null || locked.getStockQuantity() < qty) {
                        log.error("Stock depleted after SePay payment: variantId={}, needed={}, available={}",
                                variantId, qty, locked.getStockQuantity());
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        throw new OutOfStockException(
                                locked.getVariantName(), locked.getVariantName(), qty,
                                locked.getStockQuantity() != null ? locked.getStockQuantity() : 0
                        );
                    }

                    variantRepository.deductStock(variantId, qty);
                    log.info("Stock deducted (SEPAY): variantId={}, qty={}", variantId, qty);
                });

        // 5. Update Payment → SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(webhookRequest.getReferenceCode());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 6. Update Order → PAID
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // 7. Clear cart
        String buyerId = order.getBuyer().getId();
        cartItemRepository.findByCartUserIdAndVariantIds(
                buyerId,
                orderItems.stream()
                        .map(oi -> oi.getProductVariant().getId())
                        .collect(Collectors.toList())
        ).ifPresent(items -> {
            cartItemRepository.deleteAllInBatch(items);
            log.info("Cart cleared after SEPAY success: buyerId={}, items={}", buyerId, items.size());
        });

        log.info("SEPAY payment SUCCESS: orderId={}, transactionRef='{}'", order.getId(), content);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS — SHIPPING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Tính phí ship tổng (sum của tất cả shop).
     * Dùng ở previewCheckout khi chỉ cần tổng.
     */
    private BigDecimal calcTotalShippingFee(Map<Shop, List<CartItem>> itemsByShop,
                                             Integer toDistrictId, String toWardCode) {
        return calcShippingFeeByShop(itemsByShop, toDistrictId, toWardCode)
                .values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tính phí ship cho từng shop riêng biệt.
     * Kết quả là Map<Shop → shippingFee> — dùng khi tạo ShopOrder (cần lưu per-shop).
     * <p>
     * Nếu shop chưa cấu hình GHN (ghnShopId/districtId/wardCode = null) → phí = 0 + log warn.
     * Nếu GHN API lỗi → bắt exception, gán 0 + log error (không làm checkout fail).
     */
    private Map<Shop, BigDecimal> calcShippingFeeByShop(Map<Shop, List<CartItem>> itemsByShop,
                                                         Integer toDistrictId, String toWardCode) {
        Map<Shop, BigDecimal> result = new LinkedHashMap<>();

        for (Map.Entry<Shop, List<CartItem>> entry : itemsByShop.entrySet()) {
            Shop shop = entry.getKey();
            List<CartItem> shopItems = entry.getValue();

            // Kiểm tra shop có đủ thông tin GHN không
            if (shop.getGhnShopId() == null || shop.getDistrictId() == null || shop.getWardCode() == null) {
                log.warn("Shop id={} chưa cấu hình GHN (ghnShopId/districtId/wardCode) → shippingFee = 0", shop.getId());
                result.put(shop, BigDecimal.ZERO);
                continue;
            }

            // Tính tổng khối lượng của các items thuộc shop này
            int totalWeightGram = shopItems.stream()
                    .mapToInt(item -> {
                        // Lấy weight từ variant nếu có, fallback sang default
                        Integer w = item.getProductVariant().getWeight();
                        int weightPerUnit = (w != null && w > 0) ? w : DEFAULT_WEIGHT_PER_ITEM_GRAM;
                        return weightPerUnit * item.getQuantity();
                    })
                    .sum();

            try {
                int fee = shippingService.calculateShippingFee(
                        shop.getGhnShopId(),
                        shop.getDistrictId(),
                        shop.getWardCode(),
                        toDistrictId,
                        toWardCode,
                        totalWeightGram,
                        10, 10, 10,   // kích thước mặc định (cm)
                        null          // hàng nhẹ — service_type_id = 2
                );
                result.put(shop, BigDecimal.valueOf(fee));
                log.info("Shipping fee: shopId={}, weight={}g → {}đ", shop.getId(), totalWeightGram, fee);

            } catch (Exception e) {
                // GHN lỗi không nên block checkout — fallback về 0 + log
                log.error("GHN API lỗi khi tính ship cho shopId={}: {}", shop.getId(), e.getMessage());
                result.put(shop, BigDecimal.ZERO);
            }
        }

        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS — ORDER BUILDING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Tạo ShopOrder + OrderItem cho từng shop kèm shippingFee thật.
     * COD: deduct stock ngay.
     * SEPAY: KHÔNG deduct stock — chờ webhook.
     */
    private List<ShopOrderResponse> buildShopOrders(Order savedOrder,
                                                     Map<Shop, List<CartItem>> itemsByShop,
                                                     Map<Shop, BigDecimal> shippingFeeByShop,
                                                     PaymentMethod paymentMethod) {
        List<ShopOrderResponse> responses = new ArrayList<>();

        for (Map.Entry<Shop, List<CartItem>> entry : itemsByShop.entrySet()) {
            Shop shop = entry.getKey();
            List<CartItem> shopItems = entry.getValue();

            BigDecimal shopTotal = shopItems.stream()
                    .map(item -> item.getProductVariant().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Lấy phí ship thật của shop này
            BigDecimal shopShippingFee = shippingFeeByShop.getOrDefault(shop, BigDecimal.ZERO);

            OrderStatus shopOrderStatus = (paymentMethod == PaymentMethod.COD)
                    ? OrderStatus.PENDING
                    : OrderStatus.PENDING_PAYMENT;

            ShopOrder savedShopOrder = shopOrderRepository.save(ShopOrder.builder()
                    .order(savedOrder)
                    .shop(shop)
                    .status(shopOrderStatus)
                    .shopTotalAmount(shopTotal)
                    .shippingFee(shopShippingFee)   // ← lưu phí ship thật, không ZERO
                    .build());

            log.info("ShopOrder created: id={}, shopId={}, goods={}, ship={}, method={}",
                    savedShopOrder.getId(), shop.getId(), shopTotal, shopShippingFee, paymentMethod);

            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for (CartItem item : shopItems) {
                ProductVariant variant = item.getProductVariant();
                BigDecimal priceAtBuy = variant.getPrice();
                int qty = item.getQuantity();

                OrderItem savedItem = orderItemRepository.save(OrderItem.builder()
                        .shopOrder(savedShopOrder)
                        .productVariant(variant)
                        .quantity(qty)
                        .priceAtBuy(priceAtBuy)
                        .discountAmount(BigDecimal.ZERO)
                        .build());

                // COD: trừ stock ngay. SEPAY: chờ webhook
                if (paymentMethod == PaymentMethod.COD) {
                    variantRepository.deductStock(variant.getId(), qty);
                    log.info("Stock deducted (COD): variantId={}, qty={}", variant.getId(), qty);
                }

                itemResponses.add(OrderItemResponse.builder()
                        .orderItemId(savedItem.getId())
                        .variantId(variant.getId())
                        .variantName(variant.getVariantName())
                        .sku(variant.getSku())
                        .productName(item.getProduct().getProductName())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .quantity(qty)
                        .priceAtBuy(priceAtBuy)
                        .subtotal(priceAtBuy.multiply(BigDecimal.valueOf(qty)))
                        .build());
            }

            responses.add(ShopOrderResponse.builder()
                    .shopOrderId(savedShopOrder.getId())
                    .shopId(shop.getId())
                    .shopName(shop.getShopName())
                    .status(shopOrderStatus)
                    .shopTotalAmount(shopTotal)
                    .shippingFee(shopShippingFee)   // ← expose trong response
                    .items(itemResponses)
                    .build());
        }

        return responses;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS — GENERAL
    // ═══════════════════════════════════════════════════════════════════════════

    private User resolveUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private List<CartItem> fetchAndValidateCartItems(List<String> cartItemIds, User buyer) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_EMPTY_CART_ITEMS);
        }
        List<CartItem> cartItems = cartItemRepository.findByIdInWithDetails(cartItemIds);
        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        for (CartItem item : cartItems) {
            if (!item.getCart().getUser().getId().equals(buyer.getId())) {
                log.warn("Unauthorized checkout: userId={}, cartItemId={}", buyer.getId(), item.getId());
                throw new AppException(ErrorCode.ORDER_CART_ITEM_NOT_OWNED);
            }
        }
        return cartItems;
    }

    /** COD: Pessimistic Write Lock + kiểm tra stock. */
    private void lockAndValidateStock(List<CartItem> cartItems) {
        cartItems.stream()
                .sorted(Comparator.comparingLong(item -> item.getProductVariant().getId()))
                .forEach(item -> {
                    Long variantId = item.getProductVariant().getId();
                    int requestedQty = item.getQuantity();
                    ProductVariant locked = variantRepository.findByIdForUpdate(variantId)
                            .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
                    if (locked.getStockQuantity() == null || locked.getStockQuantity() < requestedQty) {
                        throw new OutOfStockException(
                                item.getProduct().getProductName(), locked.getVariantName(),
                                requestedQty, locked.getStockQuantity() != null ? locked.getStockQuantity() : 0
                        );
                    }
                });
    }

    /** SEPAY: Chỉ validate stock, không lock. Lock sẽ xảy ra trong webhook. */
    private void validateStockOnly(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            int requestedQty = item.getQuantity();
            if (variant.getStockQuantity() == null || variant.getStockQuantity() < requestedQty) {
                throw new OutOfStockException(
                        item.getProduct().getProductName(), variant.getVariantName(),
                        requestedQty, variant.getStockQuantity() != null ? variant.getStockQuantity() : 0
                );
            }
        }
    }

    private BigDecimal calcTotalAmount(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Shop, List<CartItem>> groupItemsByShop(List<CartItem> cartItems) {
        return cartItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getShop(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Tìm Payment từ nội dung chuyển khoản — flexible matching.
     * Hỗ trợ: "ORDER4", "ORDER_4", "CHUYEN KHOAN ORDER4 THANH TOAN"
     */
    private Payment findPaymentByContent(String content) {
        if (content == null || content.isBlank()) {
            throw new AppException(ErrorCode.PAYMENT_TRANSACTION_REF_NOT_FOUND);
        }

        // Bước 1: Exact match
        Optional<Payment> exact = paymentRepository.findByTransactionRef(content);
        if (exact.isPresent()) return exact.get();

        // Bước 2: Regex tách ORDER\d+
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("ORDER(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(content.replaceAll("[_\\-]", ""));

        while (matcher.find()) {
            String candidate = "ORDER" + matcher.group(1);
            Optional<Payment> found = paymentRepository.findByTransactionRef(candidate);
            if (found.isPresent()) {
                log.info("SePay: matched '{}' from content='{}'", candidate, content);
                return found.get();
            }
        }

        log.warn("SePay webhook: no matching transactionRef in content='{}'", content);
        throw new AppException(ErrorCode.PAYMENT_TRANSACTION_REF_NOT_FOUND);
    }
}
