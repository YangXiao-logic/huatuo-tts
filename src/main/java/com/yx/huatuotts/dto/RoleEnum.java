package com.yx.huatuotts.dto;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {

    SYSTEM("system"),
    ASSISTANT("assistant"),
    USER("user"),
            ;


    @JsonValue // 可以让RequestBody正确映射
//    @EnumValue // 可以让Mybatis-Plus正确映射
    private final String role;

}
