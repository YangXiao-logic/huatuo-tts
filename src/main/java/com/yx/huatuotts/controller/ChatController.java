package com.yx.huatuotts.controller;


import com.yx.huatuotts.dto.ChatAudioParam;
import com.yx.huatuotts.service.OpenAIService;
import com.yx.huatuotts.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private OpenAIService openAIService;


    @PostMapping("/audio/completion")
    @CrossOrigin
    public SseEmitter chatAudio(@RequestBody ChatAudioParam chatAudioParam) {

        SseEmitter sseEmitter = new SseEmitter(-1L);
        openAIService.chat(chatAudioParam, sseEmitter);
        return sseEmitter;
    }

    @Autowired
    private TokenService tokenService;

    @GetMapping("/token")
    public String getAuthTTSUrl() throws Exception {
        return tokenService.getToken();
    }





}
