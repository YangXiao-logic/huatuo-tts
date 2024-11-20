package com.yx.huatuotts.dto;


import lombok.Data;

@Data
public class TtsParam {



    private String voice;
    private int speedRate;
    private int volume;
    private int pitchRate;
    private int sampleRate;

    private EngineEnum engine;

}
