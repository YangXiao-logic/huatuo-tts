package com.yx.huatuotts.dto;


import lombok.Data;

import java.util.List;

@Data
public class OpenaiParam {

    String baseUrl;
    String apiKey;
    String systemPrompt;
    String model;
    private List<Message> messages;

}
