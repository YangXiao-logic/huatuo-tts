package com.yx.huatuotts.listener;

import com.alibaba.nls.client.protocol.tts.FlowingSpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.StreamInputTts;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Objects;


@Slf4j
public class AliyunTTSEventSourceListenerV1 extends EventSourceListener {


    private SseEmitter sseEmitter;
    private StringBuilder currentText = new StringBuilder();
    private ObjectMapper objectMapper = new ObjectMapper();

    FlowingSpeechSynthesizer synthesizer;

    public AliyunTTSEventSourceListenerV1(SseEmitter sseEmitter, FlowingSpeechSynthesizer synthesizer) {
        this.sseEmitter= sseEmitter;
        this.synthesizer = synthesizer;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
//        log.info("OpenAI建立sse连接...");
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {

        if (data.equals("[DONE]")) {
//            log.info("OpenAI返回数据结束了");
            return;
        }
        if (data.isEmpty()){
            return;
        }
        try {
            // 将字符串解析为JsonNode对象
            JsonNode rootNode = objectMapper.readTree(data);

            // 获取choices数组
            JsonNode choicesNode = rootNode.path("choices");

            // 假设我们知道choices数组中只有一个元素
            JsonNode firstChoiceNode = choicesNode.get(0);

            // 获取delta对象
            JsonNode deltaNode = firstChoiceNode.path("delta");

            // 最后，获取content字段的值
            String content = deltaNode.path("content").asText();

            if(content.isEmpty()){
                return;
            }

            synthesizer.send(content);

            String jsonStr = objectMapper.writeValueAsString(Map.of("dataType", "text", "content", content));
            sseEmitter.send(jsonStr);


        } catch (Exception e) {
            log.error("sse信息推送失败！");
            eventSource.cancel();
            sseEmitter.complete();
            e.printStackTrace();
        }

//        log.info("OpenAI返回数据：{}", data);

    }

    @Override
    public void onClosed(EventSource eventSource) {
//        sseEmitter.complete();
        try {
            synthesizer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("OpenAI发送结束，OpenAI发送关闭语音合成连接...");
    }

    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        sseEmitter.complete();
        if(Objects.isNull(response)){
            log.error("OpenAI  sse连接异常:{}", t);
            eventSource.cancel();
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
//            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
//            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        eventSource.cancel();
    }




}
