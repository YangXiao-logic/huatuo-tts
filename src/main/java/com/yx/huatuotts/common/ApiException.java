package com.yx.huatuotts.common;



public class ApiException extends RuntimeException {
    private ReturnCode errorCode;

    public ApiException(ReturnCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(String message) {
        super(message);
    }
    

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(ReturnCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ReturnCode getErrorCode() {
        return errorCode;
    }
}
