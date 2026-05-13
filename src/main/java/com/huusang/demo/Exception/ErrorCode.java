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
