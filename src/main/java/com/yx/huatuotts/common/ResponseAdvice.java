package com.yx.huatuotts.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        // 这里可以添加逻辑来决定是否需要处理特定的方法或消息转换器类型
        // 例如，可以检查是否是RestController的方法
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 检查响应体是否为String类型，如果是，则使用ObjectMapper序列化
//        这段代码一定要加，如果Controller直接返回String的话，SpringBoot是直接返回，故我们需要手动转换成json。
        if (body instanceof String) {
            return objectMapper.writeValueAsString(ResultData.success((String) body));
        }
        if(body instanceof ResultData){
            return body;
        }
        // 其他类型直接封装为ResultData对象
        return ResultData.success(body);
    }
}