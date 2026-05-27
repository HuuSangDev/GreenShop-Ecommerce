package com.huusang.demo.Exception;

public enum ErrorCode {
    //USER
    USER_EXISTED(101,"user existed"),
    ID_USER_NOT_FOUND(102,"id not found"),
    USER_MUST_BE_LOCKED(102,"user must be locked"),
    ROLE_NAME_NOT_FOUND(102,"role name not found"),
    USER_NOT_FOUND(102,"user not found"),
    PHONE_NUMBER_EXISTED(104,"phone number existed "),
    UNAUTHENTICATED(104,"unauthenticated "),

    //CATEGORY
    CATEGORY_NOT_FOUND(201, "Category not found"),
    CATEGORY_HAS_CHILDREN(202, "Cannot delete category that has subcategories"),
    CATEGORY_HAS_PRODUCTS(203, "Cannot delete category that has products"),
    CATEGORY_NAME_EXISTED(204, "Category name already exists under the same parent"),
    CIRCULAR_CATEGORY_REFERENCE(205, "Category cannot be its own ancestor"),

    //CART
    CART_NOT_FOUND(301, "Cart not found"),
    CART_ITEM_NOT_FOUND(302, "Cart item not found"),
    PRODUCT_NOT_FOUND(303, "Product not found"),
    VARIANT_NOT_FOUND(304, "Product variant not found"),
    OUT_OF_STOCK(305, "Product is out of stock"),
    INSUFFICIENT_STOCK(306, "Requested quantity exceeds available stock"),
    INVALID_QUANTITY(307, "Quantity must be greater than 0"),
    VARIANT_NOT_BELONG_TO_PRODUCT(308, "Variant does not belong to the specified product"),
    PRODUCT_UNAVAILABLE(309, "Product is currently unavailable"),

    //ORDER
    ORDER_CART_ITEM_NOT_OWNED(401, "Cart item does not belong to the current user"),
    ORDER_EMPTY_CART_ITEMS(402, "No cart items provided for checkout"),
    ORDER_NOT_FOUND(403, "Order not found"),
    ORDER_INVALID_PAYMENT_METHOD(404, "Invalid payment method"),

    //PAYMENT
    PAYMENT_NOT_FOUND(501, "Payment not found"),
    PAYMENT_ALREADY_PAID(502, "Order has already been paid"),
    PAYMENT_TRANSACTION_REF_NOT_FOUND(503, "Transaction reference not found"),

    //SHOP
    SHOP_NOT_FOUND(601, "Shop not found"),
    SHOP_ALREADY_EXISTS(602, "User already has a shop"),
    SHOP_APPLICATION_NOT_FOUND(603, "Shop application not found"),
    SHOP_APPLICATION_ALREADY_PENDING(604, "You already have a pending application"),
    SHOP_APPLICATION_NOT_PENDING(605, "Application is not in PENDING status"),
    SHOP_WALLET_NOT_FOUND(606, "Shop wallet not found"),
    SHOP_INSUFFICIENT_BALANCE(607, "Insufficient wallet balance for withdrawal"),
    SHOP_WITHDRAWAL_NOT_FOUND(608, "Withdrawal request not found"),
    SHOP_WITHDRAWAL_NOT_PENDING(609, "Withdrawal is not in PENDING status"),

    VOUCHER_NOT_FOUND(301, "Không tìm thấy voucher"),
    VOUCHER_CODE_EXISTED(302, "Mã voucher đã tồn tại"),
    VOUCHER_EXPIRED(303, "Voucher đã hết hạn"),
    VOUCHER_NOT_STARTED(304, "Voucher chưa đến thời gian sử dụng"),
    VOUCHER_USAGE_LIMIT_REACHED(305, "Voucher đã hết lượt sử dụng"),
    VOUCHER_MIN_ORDER_NOT_MET(306, "Giá trị đơn hàng chưa đạt mức tối thiểu để dùng voucher"),
    VOUCHER_ALREADY_USED(307, "Bạn đã sử dụng voucher này rồi"),
    VOUCHER_INACTIVE(308, "Voucher đã bị vô hiệu hóa"),
    VOUCHER_ALREADY_ACTIVE(309, "Voucher đang hoạt động"),
    VOUCHER_CANNOT_UPDATE(310, "Không thể sửa voucher đã được sử dụng"),
    UNAUTHORIZED_VOUCHER(311, "Bạn không có quyền thao tác voucher này"),

    // ADDRESS
    ADDRESS_NOT_FOUND(701, "Không tìm thấy địa chỉ"),
    ADDRESS_NOT_OWNED(702, "Địa chỉ không thuộc về bạn"),
    ADDRESS_DEFAULT_CANNOT_DELETE(703, "Không thể xóa địa chỉ mặc định. Hãy đặt địa chỉ khác làm mặc định trước"),
    ADDRESS_NO_DEFAULT(704, "Bạn chưa có địa chỉ mặc định. Vui lòng thêm địa chỉ giao hàng"),
    ;

    ErrorCode(int code, String message) {
        this.code=code;
        this.message=message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
