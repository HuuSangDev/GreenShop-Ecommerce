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
