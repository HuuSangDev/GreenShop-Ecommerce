package com.huusang.demo.Service;

import com.huusang.demo.Dto.Request.CheckoutRequest;
import com.huusang.demo.Dto.Response.OrderResponse;
import com.huusang.demo.Entity.*;
import com.huusang.demo.Enum.OrderStatus;
import com.huusang.demo.Exception.AppException;
import com.huusang.demo.Exception.ErrorCode;
import com.huusang.demo.Exception.OutOfStockException;
import com.huusang.demo.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService — Unit Tests")
class OrderServiceTest {

    @Mock CartItemRepository     cartItemRepository;
    @Mock ProductVariantRepository variantRepository;
    @Mock OrderRepository        orderRepository;
    @Mock OrderItemRepository    orderItemRepository;
    @Mock ShopOrderRepository    shopOrderRepository;
    @Mock UserRepository         userRepository;

    @InjectMocks
    OrderService orderService;

    // ─── Test Data ──────────────────────────────────────────────────────────────

    private User        buyer;
    private Shop        shop;
    private Product     product;
    private ProductVariant variant;
    private Cart        cart;
    private CartItem    cartItem;
    private Order       savedOrder;
    private ShopOrder   savedShopOrder;
    private OrderItem   savedOrderItem;

    private static final String USER_EMAIL   = "buyer@test.com";
    private static final String CART_ITEM_ID = "cart-item-uuid-001";

    @BeforeEach
    void setUp() {
        buyer = User.builder()
                .id("user-uuid-001")
                .email(USER_EMAIL)
                .username("buyer")
                .password("hashed")
                .build();

        shop = Shop.builder()
                .id(10L)
                .shopName("Shop Test")
                .build();

        product = Product.builder()
                .id(100L)
                .productName("Áo thun")
                .shop(shop)
                .available(true)
                .imageUrl("http://img.example.com/ao-thun.jpg")
                .build();

        variant = ProductVariant.builder()
                .id(200L)
                .variantName("Màu Xanh, Size M")
                .price(new BigDecimal("150000"))
                .stockQuantity(10)
                .sku("SKU-001")
                .version(1L)
                .build();

        cart = Cart.builder()
                .id("cart-uuid-001")
                .user(buyer)
                .createdAt(LocalDateTime.now())
                .build();

        cartItem = CartItem.builder()
                .id(CART_ITEM_ID)
                .cart(cart)
                .product(product)
                .productVariant(variant)
                .quantity(2)
                .priceSnapshot(new BigDecimal("150000"))
                .addedAt(LocalDateTime.now())
                .build();

        savedOrder = Order.builder()
                .id(1L)
                .buyer(buyer)
                .totalAmount(new BigDecimal("300000"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("300000"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        savedShopOrder = ShopOrder.builder()
                .id(50L)
                .order(savedOrder)
                .shop(shop)
                .status(OrderStatus.PENDING)
                .shopTotalAmount(new BigDecimal("300000"))
                .shippingFee(BigDecimal.ZERO)
                .build();

        savedOrderItem = OrderItem.builder()
                .id(500L)
                .shopOrder(savedShopOrder)
                .productVariant(variant)
                .quantity(2)
                .priceAtBuy(new BigDecimal("150000"))
                .discountAmount(BigDecimal.ZERO)
                .build();
    }

    // ─── Happy Path ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Happy Path: Checkout thành công với 1 shop, 1 sản phẩm")
    void processMultiVendorCheckout_happyPath_success() {
        // Given
        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of(CART_ITEM_ID))
                .build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByIdInWithDetails(List.of(CART_ITEM_ID)))
                .thenReturn(List.of(cartItem));
        when(variantRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(shopOrderRepository.save(any(ShopOrder.class))).thenReturn(savedShopOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);

        // When
        OrderResponse response = orderService.processMultiVendorCheckout(USER_EMAIL, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("300000");
        assertThat(response.getTotalShops()).isEqualTo(1);
        assertThat(response.getTotalItems()).isEqualTo(2); // quantity = 2
        assertThat(response.getShopOrders()).hasSize(1);
        assertThat(response.getShopOrders().get(0).getShopId()).isEqualTo(10L);
        assertThat(response.getShopOrders().get(0).getItems()).hasSize(1);

        // Verify interactions
        verify(orderRepository).save(any(Order.class));
        verify(shopOrderRepository).save(any(ShopOrder.class));
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(variantRepository).deductStock(200L, 2);
        verify(cartItemRepository).deleteAllInBatch(List.of(cartItem));
    }

    @Test
    @DisplayName("✅ Multi-Vendor: 2 CartItems từ 2 Shops → 1 Order + 2 ShopOrders")
    void processMultiVendorCheckout_twoShops_createsTwoShopOrders() {
        // Given — shop 2, product 2, variant 2, cartItem 2
        Shop shop2 = Shop.builder().id(20L).shopName("Shop 2").build();
        Product product2 = Product.builder()
                .id(101L).productName("Quần jean").shop(shop2).available(true)
                .imageUrl("http://img.example.com/quan.jpg").build();
        ProductVariant variant2 = ProductVariant.builder()
                .id(201L).variantName("Size 30").price(new BigDecimal("250000"))
                .stockQuantity(5).sku("SKU-002").version(1L).build();
        Cart cart2 = Cart.builder().id("cart-uuid-001").user(buyer).build();
        CartItem cartItem2 = CartItem.builder()
                .id("cart-item-uuid-002").cart(cart2)
                .product(product2).productVariant(variant2).quantity(1)
                .priceSnapshot(new BigDecimal("250000")).build();

        ShopOrder savedShopOrder2 = ShopOrder.builder()
                .id(51L).order(savedOrder).shop(shop2)
                .status(OrderStatus.PENDING).shopTotalAmount(new BigDecimal("250000"))
                .shippingFee(BigDecimal.ZERO).build();
        OrderItem savedOrderItem2 = OrderItem.builder()
                .id(501L).shopOrder(savedShopOrder2).productVariant(variant2)
                .quantity(1).priceAtBuy(new BigDecimal("250000")).discountAmount(BigDecimal.ZERO).build();

        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of(CART_ITEM_ID, "cart-item-uuid-002"))
                .build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByIdInWithDetails(anyList()))
                .thenReturn(List.of(cartItem, cartItem2));
        when(variantRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(variant));
        when(variantRepository.findByIdForUpdate(201L)).thenReturn(Optional.of(variant2));
        // totalAmount = 300000 + 250000 = 550000
        Order bigOrder = Order.builder().id(2L).buyer(buyer)
                .totalAmount(new BigDecimal("550000")).discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("550000")).status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now()).build();
        when(orderRepository.save(any(Order.class))).thenReturn(bigOrder);
        when(shopOrderRepository.save(any(ShopOrder.class)))
                .thenReturn(savedShopOrder)
                .thenReturn(savedShopOrder2);
        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(savedOrderItem)
                .thenReturn(savedOrderItem2);

        // When
        OrderResponse response = orderService.processMultiVendorCheckout(USER_EMAIL, request);

        // Then
        assertThat(response.getTotalShops()).isEqualTo(2);
        assertThat(response.getShopOrders()).hasSize(2);
        assertThat(response.getTotalItems()).isEqualTo(3); // 2 + 1

        verify(shopOrderRepository, times(2)).save(any(ShopOrder.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(variantRepository).deductStock(200L, 2);
        verify(variantRepository).deductStock(201L, 1);
    }

    // ─── Out of Stock ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("❌ Hết hàng: stockQuantity < quantity → throw OutOfStockException → 400")
    void processMultiVendorCheckout_outOfStock_throwsOutOfStockException() {
        // Given — stock chỉ còn 1, nhưng yêu cầu 2
        ProductVariant lowStockVariant = ProductVariant.builder()
                .id(200L).variantName("Màu Xanh, Size M").price(new BigDecimal("150000"))
                .stockQuantity(1).sku("SKU-001").version(1L).build();

        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of(CART_ITEM_ID))
                .build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByIdInWithDetails(List.of(CART_ITEM_ID)))
                .thenReturn(List.of(cartItem));
        when(variantRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(lowStockVariant));

        // When / Then
        assertThatThrownBy(() ->
                orderService.processMultiVendorCheckout(USER_EMAIL, request))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("Áo thun")
                .hasMessageContaining("Màu Xanh, Size M");

        // Verify order không được tạo
        verify(orderRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    @DisplayName("❌ Hết hàng hoàn toàn: stockQuantity = 0 → throw OutOfStockException")
    void processMultiVendorCheckout_zeroStock_throwsOutOfStockException() {
        ProductVariant zeroStock = ProductVariant.builder()
                .id(200L).variantName("Size S").price(new BigDecimal("150000"))
                .stockQuantity(0).sku("SKU-001").version(1L).build();

        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of(CART_ITEM_ID)).build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByIdInWithDetails(anyList())).thenReturn(List.of(cartItem));
        when(variantRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(zeroStock));

        assertThatThrownBy(() ->
                orderService.processMultiVendorCheckout(USER_EMAIL, request))
                .isInstanceOf(OutOfStockException.class);

        verify(orderRepository, never()).save(any());
    }

    // ─── Validation ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("❌ Cart item không thuộc user → throw AppException(ORDER_CART_ITEM_NOT_OWNED)")
    void processMultiVendorCheckout_cartItemNotOwnedByUser_throwsAppException() {
        // Given — cart item thuộc user khác
        User otherUser = User.builder().id("other-user-uuid").build();
        Cart otherCart = Cart.builder().id("other-cart-id").user(otherUser).build();
        CartItem foreignItem = CartItem.builder()
                .id(CART_ITEM_ID).cart(otherCart).product(product)
                .productVariant(variant).quantity(1)
                .priceSnapshot(new BigDecimal("150000")).build();

        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of(CART_ITEM_ID)).build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByIdInWithDetails(anyList())).thenReturn(List.of(foreignItem));

        // When / Then
        assertThatThrownBy(() ->
                orderService.processMultiVendorCheckout(USER_EMAIL, request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException appEx = (AppException) e;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.ORDER_CART_ITEM_NOT_OWNED);
                });

        verify(variantRepository, never()).findByIdForUpdate(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Danh sách cartItemIds rỗng → throw AppException(ORDER_EMPTY_CART_ITEMS)")
    void processMultiVendorCheckout_emptyCartItemIds_throwsAppException() {
        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of()).build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buyer));

        assertThatThrownBy(() ->
                orderService.processMultiVendorCheckout(USER_EMAIL, request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException appEx = (AppException) e;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.ORDER_EMPTY_CART_ITEMS);
                });
    }

    @Test
    @DisplayName("❌ User không tồn tại → throw AppException(USER_NOT_FOUND)")
    void processMultiVendorCheckout_userNotFound_throwsAppException() {
        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of(CART_ITEM_ID)).build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.processMultiVendorCheckout(USER_EMAIL, request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    AppException appEx = (AppException) e;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                });
    }

    // ─── Price locking ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ Giá chốt theo variant.getPrice() — không phải priceSnapshot trong giỏ")
    void processMultiVendorCheckout_priceLockedFromVariant_notSnapshot() {
        // Given — giá variant thay đổi từ 150k → 200k, priceSnapshot vẫn là 150k
        ProductVariant updatedPriceVariant = ProductVariant.builder()
                .id(200L).variantName("Màu Xanh, Size M").price(new BigDecimal("200000"))
                .stockQuantity(10).sku("SKU-001").version(2L).build();

        // cartItem.priceSnapshot = 150000 (cũ), variant.price = 200000 (mới)
        CartItem itemWithOldSnapshot = CartItem.builder()
                .id(CART_ITEM_ID).cart(cart).product(product)
                .productVariant(updatedPriceVariant).quantity(1)
                .priceSnapshot(new BigDecimal("150000")).build();

        CheckoutRequest request = CheckoutRequest.builder()
                .cartItemIds(List.of(CART_ITEM_ID)).build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(buyer));
        when(cartItemRepository.findByIdInWithDetails(anyList())).thenReturn(List.of(itemWithOldSnapshot));
        when(variantRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(updatedPriceVariant));

        Order orderWithNewPrice = Order.builder().id(1L).buyer(buyer)
                .totalAmount(new BigDecimal("200000")).discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("200000")).status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now()).build();
        when(orderRepository.save(any(Order.class))).thenReturn(orderWithNewPrice);
        when(shopOrderRepository.save(any(ShopOrder.class))).thenReturn(savedShopOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);

        // When
        OrderResponse response = orderService.processMultiVendorCheckout(USER_EMAIL, request);

        // Then — tổng tiền phải tính theo giá variant mới (200k), không phải snapshot (150k)
        assertThat(response.getTotalAmount()).isEqualByComparingTo("200000");

        // Verify OrderItem được save với priceAtBuy = 200000 (từ variant)
        verify(orderItemRepository).save(argThat(item ->
                item.getPriceAtBuy().compareTo(new BigDecimal("200000")) == 0
        ));
    }
}
