package com.yx.huatuotts.common;



/**
 * 断言处理类，用于抛出各种API异常
 * Created by macro on 2020/2/27.
 */
public class Asserts {
    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail(ReturnCode errorCode, String message) {
        throw new ApiException(errorCode, message);
    }
}
