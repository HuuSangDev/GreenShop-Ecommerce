package com.huusang.demo.Exception;

public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    public AppException(ErrorCode erorrCode) {
        super(erorrCode.getMessage());
        this.errorCode=erorrCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode erorrCode) {
        this.errorCode = erorrCode;
    }
}
