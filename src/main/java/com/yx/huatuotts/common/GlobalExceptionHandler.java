package com.yx.huatuotts.common;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 * Created by macro on 2020/2/27.
 */
@ControllerAdvice
public class GlobalExceptionHandler {



    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public ResultData handle(ApiException e) {
        if (e.getErrorCode() != null) {
            return ResultData.fail(e.getErrorCode().getCode(), e.getMessage());
        }
        return ResultData.fail(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultData handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasFieldErrors()) {
//            message = bindingResult.getGlobalErrors().get(0).getDefaultMessage();
            message = bindingResult.getFieldError().getField()+bindingResult.getFieldError().getDefaultMessage();
        }
        if(bindingResult.hasGlobalErrors()){
            message = bindingResult.getGlobalErrors().get(0).getDefaultMessage();
        }
        return ResultData.fail(message);
    }

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public ResultData handleValidException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasFieldErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField()+fieldError.getDefaultMessage();
            }
        }
        return ResultData.fail(message);
    }
}
