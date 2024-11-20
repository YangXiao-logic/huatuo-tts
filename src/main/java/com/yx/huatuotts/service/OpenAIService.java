package com.yx.huatuotts.service;


import com.alibaba.nls.client.protocol.tts.FlowingSpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.StreamInputTts;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.BaseMessage;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import com.yx.huatuotts.dto.ChatAudioParam;
import com.yx.huatuotts.dto.EngineEnum;
import com.yx.huatuotts.dto.OpenaiParam;
import com.yx.huatuotts.listener.AliyunTTSEventSourceListener;
import com.yx.huatuotts.listener.AliyunTTSEventSourceListenerV1;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import okhttp3.sse.EventSourceListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class OpenAIService {


    @Autowired
    private AliYunTTSService aliYunTTSService;

    @Autowired
    private AliYunTTSServiceV1 aliYunTTSServiceV1;

    @Value("${openaiParam.baseUrl}")
    private String baseUrl;

    @Value("${openaiParam.apiKey}")
    private String apiKey;

    @Value("${openaiParam.systemPrompt}")
    private String systemPrompt;

    @Value("${openaiParam.model}")
    private String model;

    public String chat(ChatAudioParam chatAudioParam, SseEmitter sseEmitter) {

        OpenaiParam openaiParam = chatAudioParam.getOpenaiParam();

//        http://61.241.103.33:8900/
        String baseUrl = openaiParam.getBaseUrl();
        if(baseUrl == null || baseUrl.isEmpty()){
            baseUrl = this.baseUrl;
        }
        String systemPrompt = openaiParam.getSystemPrompt();
        if(systemPrompt == null || systemPrompt.isEmpty()){
            systemPrompt = this.systemPrompt;
        }
        String model = openaiParam.getModel();
        if(model == null || model.isEmpty()){
            model = this.model;
        }
        String apiKey = openaiParam.getApiKey();
        if(apiKey == null || apiKey.isEmpty()){
            apiKey = this.apiKey;
        }

        List<com.yx.huatuotts.dto.Message> messagesParam = openaiParam.getMessages();
        List<com.unfbx.chatgpt.entity.chat.Message> messages = new ArrayList<>();

        com.unfbx.chatgpt.entity.chat.Message messageBuild =
                com.unfbx.chatgpt.entity.chat.Message.builder().role(BaseMessage.Role.SYSTEM).
                        content(systemPrompt).build();
        messages.add(messageBuild);

        messagesParam.stream().forEach(message -> {
            com.unfbx.chatgpt.entity.chat.Message build =
                    com.unfbx.chatgpt.entity.chat.Message.builder().role(message.getRole().getRole()).content(message.getContent()).build();
            messages.add(build);
        });


        //创建流式输出客户端
        OpenAiStreamClient client = OpenAiStreamClient.builder()
                .apiHost(baseUrl)
                .apiKey(Arrays.asList(apiKey))
                .build();
        //聊天
        EngineEnum engine = chatAudioParam.getTtsParam().getEngine();

        EventSourceListener eventSourceListener = null;
        FlowingSpeechSynthesizer flowingSpeechSynthesizer;
        StreamInputTts streamInputTts;
        if(engine == EngineEnum.V1 || engine == null){
            streamInputTts = null;
            flowingSpeechSynthesizer = aliYunTTSServiceV1.synthesizeAudio(sseEmitter, chatAudioParam.getTtsParam());
            eventSourceListener = new AliyunTTSEventSourceListenerV1(sseEmitter, flowingSpeechSynthesizer);
        } else {
            flowingSpeechSynthesizer = null;
            if (engine == EngineEnum.V2) {
                streamInputTts =
                        aliYunTTSService.synthesizeAudio(sseEmitter, chatAudioParam.getTtsParam());
                eventSourceListener = new AliyunTTSEventSourceListener(sseEmitter, streamInputTts);
            } else {
                streamInputTts = null;
            }
        }


//        Message userMessage = Message.builder().role(Message.Role.USER).content(question).build();
//        Message sysMessage = Message.builder().role(Message.Role.SYSTEM).content("你是一个数字人，请尽量说话口语化，并且不要说太长").build();
        ChatCompletion chatCompletion =
                ChatCompletion.builder()
                        .model(model)
                        .messages(messages).build();
        client.streamChatCompletion(chatCompletion, eventSourceListener);

        sseEmitter.onCompletion(() -> {
            log.info("sseEmitter关闭");
            if (null != flowingSpeechSynthesizer) {
                flowingSpeechSynthesizer.close();
            } else if (null != streamInputTts) {
                streamInputTts.close();
            }
//            if(flowingSpeechSynthesizer != null){
//                try {
//                    flowingSpeechSynthesizer.stop();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if(streamInputTts != null){
//                try {
//                    streamInputTts.stopStreamInputTts();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            finalEventSourceListener.getLlmEventSource().cancel();
//            sseEmitterService.cleanSseEmitter(chatId);
        });
        sseEmitter.onError((e) -> {
            log.error("sseEmitter error", e);
            if (null != flowingSpeechSynthesizer) {
                flowingSpeechSynthesizer.close();
            } else if (null != streamInputTts) {
                streamInputTts.close();
            }
//            synthesizer.stop();
//            eventSourceListener.getLlmEventSource().cancel();
//            sseEmitterService.cleanSseEmitter(chatId);
        });
        return "Hello, ";
    }


}
