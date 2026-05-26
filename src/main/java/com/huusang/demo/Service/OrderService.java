package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.CheckoutPreviewRequest;
import com.huusang.demo.Dto.Request.CheckoutRequest;
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

    CartItemRepository       cartItemRepository;
    ProductVariantRepository variantRepository;
    OrderRepository          orderRepository;
    OrderItemRepository      orderItemRepository;
    ShopOrderRepository      shopOrderRepository;
    PaymentRepository        paymentRepository;
    UserRepository           userRepository;
    SePayService             sePayService;

    // ═══════════════════════════════════════════════════════════════════════════
    // STEP 0 — CHECKOUT PREVIEW
    // POST /api/v1/checkouts/preview
    // KHÔNG tạo order, KHÔNG trừ stock, KHÔNG tạo payment.
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Tính toán và trả về thông tin xác nhận đơn hàng trước khi user đặt.
     * Read-only — không gây side effect nào với DB ngoài việc đọc.
     *
     * @param userEmail JWT subject (email)
     * @param request   danh sách cartItemIds
     * @return CheckoutPreviewResponse
     */
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse previewCheckout(String userEmail, CheckoutPreviewRequest request) {

        User buyer = resolveUser(userEmail);
        List<CartItem> cartItems = fetchAndValidateCartItems(request.getCartItemIds(), buyer);

        // Tính toán — backend tự tính, không trust frontend
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

        BigDecimal shippingFee    = BigDecimal.ZERO; // TODO: tích hợp shipping service
        BigDecimal discountAmount = BigDecimal.ZERO; // TODO: tích hợp voucher
        BigDecimal finalAmount    = subtotal.add(shippingFee).subtract(discountAmount);

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
     *
     * @param userEmail     email từ JWT subject
     * @param request       cartItemIds + paymentMethod
     * @return OrderResponse (COD: shopOrders / SEPAY: thêm paymentUrl)
     */
    @Transactional
    public OrderResponse processMultiVendorCheckout(String userEmail, CheckoutRequest request) {

        // ─── 0. Resolve user ────────────────────────────────────────────────────────
        User buyer = resolveUser(userEmail);

        // ─── 1. Lấy giỏ hàng & validate ownership ──────────────────────────────────
        List<CartItem> cartItems = fetchAndValidateCartItems(request.getCartItemIds(), buyer);

        // ─── 2. Kiểm tra tồn kho (+ lock nếu COD) ──────────────────────────────────
        PaymentMethod paymentMethod = request.getPaymentMethod();

        if (paymentMethod == PaymentMethod.COD) {
            // COD: Pessimistic Write Lock → tránh oversell ngay tại đây
            lockAndValidateStock(cartItems);
        } else if (paymentMethod == PaymentMethod.SEPAY) {
            // SEPAY: Chỉ validate stock (không lock vì chưa thanh toán)
            // Stock sẽ được lock + deduct trong webhook callback sau khi thanh toán thành công
            validateStockOnly(cartItems);
        } else {
            throw new AppException(ErrorCode.ORDER_INVALID_PAYMENT_METHOD);
        }

        // ─── 3. Tính totalAmount — backend tự tính, không trust frontend ───────────
        BigDecimal totalAmount = calcTotalAmount(cartItems);

        // ─── 4. Tạo Order ────────────────────────────────────────────────────────────
        OrderStatus initialStatus = (paymentMethod == PaymentMethod.COD)
                ? OrderStatus.PENDING
                : OrderStatus.PENDING_PAYMENT;

        Order savedOrder = orderRepository.save(Order.builder()
                .buyer(buyer)
                .totalAmount(totalAmount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .status(initialStatus)
                .paymentMethod(paymentMethod.name())
                .build());

        log.info("Order created: orderId={}, buyer={}, method={}, amount={}",
                savedOrder.getId(), buyer.getId(), paymentMethod, totalAmount);

        // ─── 5. Group by shop → tạo ShopOrder + OrderItem ───────────────────────────
        Map<Shop, List<CartItem>> itemsByShop = groupItemsByShop(cartItems);
        List<ShopOrderResponse> shopOrderResponses = buildShopOrders(savedOrder, itemsByShop, paymentMethod);

        // ─── 6. Xử lý theo paymentMethod ────────────────────────────────────────────
        String paymentUrl = null;

        if (paymentMethod == PaymentMethod.COD) {
            // COD: xóa giỏ hàng ngay vì stock đã trừ
            cartItemRepository.deleteAllInBatch(cartItems);
            log.info("COD checkout done: orderId={}, cartItemsDeleted={}", savedOrder.getId(), cartItems.size());

        } else {
            // transactionRef = "ORDER" + id (KHÔNG có dấu _ để tránh ngân hàng lọc ký tự đặc biệt)
            // QR content: user sẽ thấy "ORDER4" trong nội dung chuyển khoản
            String transactionRef = "ORDER" + savedOrder.getId();
            paymentUrl = sePayService.generateQrUrl(totalAmount, transactionRef);

            paymentRepository.save(Payment.builder()
                    .order(savedOrder)
                    .method(PaymentMethod.SEPAY)
                    .status(PaymentStatus.PENDING)
                    .amount(totalAmount)
                    .transactionRef(transactionRef)
                    .checkoutUrl(paymentUrl)
                    .build());

            // KHÔNG xóa giỏ hàng — giỏ chỉ bị xóa sau payment SUCCESS (trong webhook)
            log.info("SEPAY checkout created: orderId={}, transactionRef={}", savedOrder.getId(), transactionRef);
        }

        // ─── 7. Build và trả về response ────────────────────────────────────────────
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .status(initialStatus)
                .paymentMethod(paymentMethod)
                .totalAmount(totalAmount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .createdAt(savedOrder.getCreatedAt())
                .totalShops(shopOrderResponses.size())
                .totalItems(totalItems)
                .shopOrders(shopOrderResponses)
                .paymentUrl(paymentUrl) // null nếu COD
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STEP 2 — SEPAY WEBHOOK CALLBACK
    // POST /api/v1/payments/sepay/webhook  (public, không cần JWT)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý callback từ SePay khi giao dịch thành công.
     * <p>
     * Flow:
     * 1. Parse transactionRef từ content field
     * 2. Tìm Payment theo transactionRef
     * 3. Verify amount khớp
     * 4. Pessimistic lock + Deduct stock
     * 5. Update Payment → SUCCESS, Order → PAID
     * 6. Xóa CartItems
     */
    @Transactional
    public void handleSePayCallback(SePayWebhookRequest webhookRequest) {
        String content = webhookRequest.getContent();
        log.info("SePay webhook received: content='{}', amount={}", content, webhookRequest.getTransferAmount());

        // 1. Tìm Payment theo transactionRef
        // SePay (hoặc ngân hàng trung gian) đôi khi tự lọc ký tự đặc biệt trong nội dung:
        //   "ORDER_4" → "ORDER4", hoặc thêm text thừa: "CHUYEN KHOAN ORDER4 THANH TOAN"
        // Strategy: tách lấy token dạng ORDER\d+ từ content → tìm payment
        Payment payment = findPaymentByContent(content);

        // 2. Idempotency check — tránh xử lý 2 lần nếu SePay gửi webhook duplicate
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("SePay webhook: payment already processed, transactionRef='{}'", content);
            return; // Idempotent — không làm gì, return 200 để SePay không retry
        }

        Order order = payment.getOrder();

        // 3. Verify amount — backend tự tính, không trust webhook amount
        if (webhookRequest.getTransferAmount().compareTo(payment.getAmount()) < 0) {
            log.error("SePay webhook: amount mismatch. Expected={}, received={}",
                    payment.getAmount(), webhookRequest.getTransferAmount());
            // Không throw — mark FAILED và return (SePay không cần retry)
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return;
        }

        // 4. Lấy cart items của order này để deduct stock
        // OrderItem → variant → deduct
        List<OrderItem> orderItems = orderItemRepository.findByShopOrderOrderId(order.getId());

        // Lock theo thứ tự ID tăng dần để tránh deadlock
        orderItems.stream()
                .sorted(Comparator.comparingLong(oi -> oi.getProductVariant().getId()))
                .forEach(orderItem -> {
                    Long variantId = orderItem.getProductVariant().getId();
                    int qty = orderItem.getQuantity();

                    // Pessimistic Write Lock
                    ProductVariant locked = variantRepository.findByIdForUpdate(variantId)
                            .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                    // Kiểm tra lại stock — có thể bị mua hết trong lúc chờ thanh toán
                    if (locked.getStockQuantity() == null || locked.getStockQuantity() < qty) {
                        log.error("Stock depleted after SePay payment: variantId={}, needed={}, available={}",
                                variantId, qty, locked.getStockQuantity());
                        // TODO: Trigger refund flow. Hiện tại mark FAILED.
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        throw new OutOfStockException(
                                locked.getVariantName(), locked.getVariantName(), qty,
                                locked.getStockQuantity() != null ? locked.getStockQuantity() : 0
                        );
                    }

                    // Trừ tồn kho
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

        // 7. Xóa CartItems — giỏ hàng chỉ clear sau khi thanh toán thành công
        // Lấy lại cart items từ buyer's cart (items vẫn còn vì chưa xóa lúc checkout)
        String buyerId = order.getBuyer().getId();
        // Tìm cartItems qua buyer cart để xóa
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
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Resolve user từ email, ném AppException nếu không tìm thấy. */
    private User resolveUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Fetch cart items với JOIN FETCH (tránh N+1), validate IDs không rỗng,
     * validate tất cả items thuộc về đúng buyer.
     */
    private List<CartItem> fetchAndValidateCartItems(List<String> cartItemIds, User buyer) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_EMPTY_CART_ITEMS);
        }

        List<CartItem> cartItems = cartItemRepository.findByIdInWithDetails(cartItemIds);

        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        // Validate ownership — không để user checkout item của người khác
        for (CartItem item : cartItems) {
            if (!item.getCart().getUser().getId().equals(buyer.getId())) {
                log.warn("Unauthorized checkout attempt: userId={}, cartItemId={}",
                        buyer.getId(), item.getId());
                throw new AppException(ErrorCode.ORDER_CART_ITEM_NOT_OWNED);
            }
        }

        return cartItems;
    }

    /**
     * COD path: Pessimistic Write Lock + kiểm tra stock.
     * Lock theo thứ tự ID tăng dần → tránh deadlock giữa concurrent transactions.
     */
    private void lockAndValidateStock(List<CartItem> cartItems) {
        cartItems.stream()
                .sorted(Comparator.comparingLong(item -> item.getProductVariant().getId()))
                .forEach(item -> {
                    Long variantId = item.getProductVariant().getId();
                    int requestedQty = item.getQuantity();

                    // SELECT * FROM product_variants WHERE id = ? FOR UPDATE
                    ProductVariant locked = variantRepository.findByIdForUpdate(variantId)
                            .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                    if (locked.getStockQuantity() == null || locked.getStockQuantity() < requestedQty) {
                        log.warn("Out of stock (COD): variantId={}, requested={}, available={}",
                                variantId, requestedQty,
                                locked.getStockQuantity() != null ? locked.getStockQuantity() : 0);
                        throw new OutOfStockException(
                                item.getProduct().getProductName(),
                                locked.getVariantName(),
                                requestedQty,
                                locked.getStockQuantity() != null ? locked.getStockQuantity() : 0
                        );
                    }
                });
    }

    /**
     * SEPAY path: Chỉ kiểm tra stock (không lock).
     * Lock + deduct sẽ xảy ra trong handleSePayCallback() sau khi payment thành công.
     * Anti-oversell thực sự diễn ra trong webhook transaction.
     */
    private void validateStockOnly(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            int requestedQty = item.getQuantity();
            if (variant.getStockQuantity() == null || variant.getStockQuantity() < requestedQty) {
                log.warn("Stock insufficient (SEPAY preview): variantId={}, requested={}, available={}",
                        variant.getId(), requestedQty, variant.getStockQuantity());
                throw new OutOfStockException(
                        item.getProduct().getProductName(),
                        variant.getVariantName(),
                        requestedQty,
                        variant.getStockQuantity() != null ? variant.getStockQuantity() : 0
                );
            }
        }
    }

    /** Tính tổng tiền từ giá variant hiện tại — backend tự tính, không trust frontend. */
    private BigDecimal calcTotalAmount(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tạo ShopOrder + OrderItem cho từng shop.
     * COD: deduct stock ngay trong loop.
     * SEPAY: KHÔNG deduct stock (chỉ record OrderItem để lưu lịch sử giá).
     */
    private List<ShopOrderResponse> buildShopOrders(Order savedOrder,
                                                    Map<Shop, List<CartItem>> itemsByShop,
                                                    PaymentMethod paymentMethod) {
        List<ShopOrderResponse> responses = new ArrayList<>();

        for (Map.Entry<Shop, List<CartItem>> entry : itemsByShop.entrySet()) {
            Shop shop = entry.getKey();
            List<CartItem> shopItems = entry.getValue();

            BigDecimal shopTotal = shopItems.stream()
                    .map(item -> item.getProductVariant().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            OrderStatus shopOrderStatus = (paymentMethod == PaymentMethod.COD)
                    ? OrderStatus.PENDING
                    : OrderStatus.PENDING_PAYMENT;

            // Lưu ShopOrder trước — OrderItem cần FK shopOrder_id
            ShopOrder savedShopOrder = shopOrderRepository.save(ShopOrder.builder()
                    .order(savedOrder)
                    .shop(shop)
                    .status(shopOrderStatus)
                    .shopTotalAmount(shopTotal)
                    .shippingFee(BigDecimal.ZERO)
                    .build());

            log.info("ShopOrder created: id={}, shopId={}, method={}", savedShopOrder.getId(), shop.getId(), paymentMethod);

            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for (CartItem item : shopItems) {
                ProductVariant variant = item.getProductVariant();
                BigDecimal priceAtBuy = variant.getPrice(); // chốt giá từ variant
                int qty = item.getQuantity();

                // Lưu OrderItem — chốt lịch sử giá
                OrderItem savedItem = orderItemRepository.save(OrderItem.builder()
                        .shopOrder(savedShopOrder)
                        .productVariant(variant)
                        .quantity(qty)
                        .priceAtBuy(priceAtBuy)
                        .discountAmount(BigDecimal.ZERO)
                        .build());

                // COD: trừ stock ngay. SEPAY: KHÔNG trừ — chờ webhook
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
                    .items(itemResponses)
                    .build());
        }

        return responses;
    }

    /** Group CartItems theo Shop — LinkedHashMap giữ nguyên thứ tự (deterministic). */
    private Map<Shop, List<CartItem>> groupItemsByShop(List<CartItem> cartItems) {
        return cartItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getShop(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Tìm Payment từ nội dung chuyển khoản (content field của webhook).
     * <p>
     * Ngân hàng / SePay có thể biến đổi nội dung:
     *   "ORDER4"            → exact match với transactionRef "ORDER4"
     *   "ORDER_4"           → cũ, vẫn hỗ trợ để backward compat
     *   "CHUYEN KHOAN ORDER4 THANH TOAN"  → tách token ORDER4 ra
     * <p>
     * Thuật toán:
     *   1. Exact match trước
     *   2. Dùng regex tìm token ORDER\d+ trong content → tìm theo token đó
     */
    private Payment findPaymentByContent(String content) {
        if (content == null || content.isBlank()) {
            log.warn("SePay webhook: empty content");
            throw new AppException(ErrorCode.PAYMENT_TRANSACTION_REF_NOT_FOUND);
        }

        // Bước 1: Exact match (content = "ORDER4" hoặc "ORDER_4")
        Optional<Payment> exact = paymentRepository.findByTransactionRef(content);
        if (exact.isPresent()) {
            return exact.get();
        }

        // Bước 2: Tách token ORDER\d+ từ nội dung tự do
        // Ví dụ: "CHUYEN KHOAN ORDER4 THANH TOAN ORDER DAT HANG" → tìm "ORDER4"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("ORDER(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(content.replaceAll("[_\\-]", ""));

        while (matcher.find()) {
            String candidate = "ORDER" + matcher.group(1); // ví dụ: "ORDER4"
            Optional<Payment> found = paymentRepository.findByTransactionRef(candidate);
            if (found.isPresent()) {
                log.info("SePay webhook: matched transactionRef='{}' from content='{}'", candidate, content);
                return found.get();
            }
        }

        log.warn("SePay webhook: no matching transactionRef in content='{}'", content);
        throw new AppException(ErrorCode.PAYMENT_TRANSACTION_REF_NOT_FOUND);
    }
}

