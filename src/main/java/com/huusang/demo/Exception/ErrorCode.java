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


    //ORDER


    //PRODUCT
    PRODUCT_NOT_FOUND(201, "Không tìm thấy sản phẩm"),
    PRODUCT_VARIANT_NOT_FOUND(202, "Không tìm thấy variant"),
    SHOP_NOT_FOUND(203, "Không tìm thấy shop"),
    CATEGORY_NOT_FOUND(204, "Không tìm thấy danh mục"),
    SKU_EXISTED(205, "SKU đã tồn tại"),
    VARIANT_IN_ACTIVE_ORDER(206, "Không thể xóa variant đang có trong đơn hàng chưa hoàn thành"),
    UNAUTHORIZED_PRODUCT(207, "Bạn không có quyền thao tác sản phẩm này"),
    SHOP_NOT_OWNED(208, "Bạn chưa có shop"),

    //VOUCHER
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
