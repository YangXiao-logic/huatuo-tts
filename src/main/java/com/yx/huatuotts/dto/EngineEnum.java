package com.yx.huatuotts.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EngineEnum {

    V1("v1"),
    V2("v2"),
            ;


    @JsonValue // 可以让RequestBody正确映射
//    @EnumValue // 可以让Mybatis-Plus正确映射
    private final String engine;

}
