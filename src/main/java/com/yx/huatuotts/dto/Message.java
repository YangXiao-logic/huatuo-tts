package com.yx.huatuotts.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {

    private RoleEnum role;
    private String content;

}
