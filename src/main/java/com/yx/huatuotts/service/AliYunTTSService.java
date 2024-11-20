package com.yx.huatuotts.service;

import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.tts.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.huatuotts.dto.TtsParam;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * 此示例演示了：
 * 流入实时语音合成API调用。
 */
@Service
public class AliYunTTSService {
    private static final Logger logger = LoggerFactory.getLogger(AliYunTTSService.class);
    private static long startTime;

    private long tokenExpirationTime;

    private NlsClient nlsClient;

    @Value("${aliyun.app_key_cn}")
    private String APPKEY_CN;

    @Autowired
    private TokenService tokenService;

    @PostConstruct
    public void init() {
        // 在这里执行初始化代码
        try {
            String url = "wss://nls-gateway-cn-beijing.aliyuncs.com/ws/v1";
            String token = tokenService.getToken();
            nlsClient = new NlsClient(url, token);

        } catch (IOException e) {
            logger.error("Failed to initialize FlowingSpeechSynthesizerService: {}", e.getMessage());
        }
    }

    public StreamInputTts synthesizeAudio(SseEmitter emitter, TtsParam ttsParam) {
        StreamInputTtsListener listener = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            listener = new StreamInputTtsListener() {

                private boolean firstRecvBinary = true;

                @Override
                //流入语音合成开始
                public void onSynthesisStart(StreamInputTtsResponse response) {
                    System.out.println("name: " + response.getName() +
                            ", status: " + response.getStatus());
                }

                @Override
                //服务端检测到了一句话的开始
                public void onSentenceBegin(StreamInputTtsResponse response) {
                    System.out.println("name: " + response.getName() +
                            ", status: " + response.getStatus());

                }

                @Override
                //服务端检测到了一句话的结束，获得这句话的起止位置和所有时间戳
                public void onSentenceEnd(StreamInputTtsResponse response) {
                    System.out.println("name: " + response.getName() +
                            ", status: " + response.getStatus() + ", subtitles: " + response.getObject("subtitles"));

                }

                //流入语音合成结束
                @Override
                public void onSynthesisComplete(StreamInputTtsResponse response) {
                    // 调用onSynthesisComplete时，表示所有TTS数据已经接收完成，所有文本都已经合成音频并返回。
                    System.out.println("name: " + response.getName() + ", status: " + response.getStatus());
//                    try {
//                        emitter.send(SseEmitter.event().name("audioEnd").data("Audio generated End."));
//
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
                    emitter.complete(); // 关闭 SSE 连接
                }

                //收到语音合成的语音二进制数据
                @Override
                public void onAudioData(ByteBuffer message) {
                    try {
                        if (firstRecvBinary) {
                            // 此处计算首包语音流的延迟，收到第一包语音流时，即可以进行语音播放，以提升响应速度（特别是实时交互场景下）。
                            firstRecvBinary = false;
//                            long now = System.currentTimeMillis();
//                            logger.info("tts first latency : " + (now - startTime) +
//                                    " ms");
                        }
                        byte[] bytesArray = new byte[message.remaining()];
                        message.get(bytesArray, 0, bytesArray.length);
//                        emitter.send(bytesArray);
                        String base64String = Base64.getEncoder().encodeToString(bytesArray);
                        String jsonStr = objectMapper.writeValueAsString(Map.of("dataType", "audio", "content",
                                base64String));

                        emitter.send(jsonStr);
//                        System.out.println("write array:" + bytesArray.length);
//                        fout.write(bytesArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //收到语音合成的增量音频时间戳
                @Override
                public void onSentenceSynthesis(StreamInputTtsResponse response) {
//                    System.out.println("name: " + response.getName() +
//                            ", status: " + response.getStatus() + ", subtitles: " + response.getObject("subtitles"));
                }

                @Override
                public void onFail(StreamInputTtsResponse response) {
                    // task_id是调用方和服务端通信的唯一标识，当遇到问题时，需要提供此task_id以便排查。

                    System.out.println(
                            "session_id: " + getStreamInputTts().getCurrentSessionId() +
                                    ", task_id: " + response.getTaskId() +
                                    //状态码
                                    ", status: " + response.getStatus() +
                                    //错误信息
                                    ", status_text: " + response.getStatusText());
                    emitter.complete();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        StreamInputTts synthesizer = null;
        try {
            //创建实例，建立连接。
            String token = tokenService.getToken();

            nlsClient.setToken(token);
//            if (tokenService.isTokenExpired()) {
//
//            }
            synthesizer = new StreamInputTts(nlsClient, listener);

            String APPKEY = APPKEY_CN;
            String voicer = ttsParam.getVoice();
//            if(langEnum == LangEnum.ENUM1){
//                APPKEY=APPKEY_EN;
//                voicer="Andy";
//            }else if(langEnum == LangEnum.ENUM2){
//                APPKEY=APPKEY_CN;
//                voicer="jielidou";
//            }else if(langEnum == LangEnum.ENUM3){
//                APPKEY=APPKEY_ID;
//                voicer="Indah";
//            }

            synthesizer.setAppKey(APPKEY);
            //设置返回音频的编码格式。
            synthesizer.setFormat(OutputFormatEnum.WAV);
            //设置返回音频的采样率。
            synthesizer.setSampleRate(ttsParam.getSampleRate());
            //发音人。注意Java SDK不支持调用超高清场景对应的发音人（例如"zhiqi"），如需调用请使用restfulAPI方式。
            synthesizer.setVoice(voicer);
            //音量，范围是0~100，可选，默认50。
            synthesizer.setVolume(ttsParam.getVolume());
            //语调，范围是-500~500，可选，默认是0。
            synthesizer.setPitchRate(ttsParam.getPitchRate());
            //语速，范围是-500~500，默认是0。
            synthesizer.setSpeechRate(ttsParam.getSpeedRate());
            //此方法将以上参数设置序列化为JSON发送给服务端，并等待服务端确认。
//            long start = System.currentTimeMillis();
            synthesizer.startStreamInputTts();
//            logger.info("tts start latency " + (System.currentTimeMillis() - start) + " ms");
//            FlowingSpeechSynthesizerService.startTime = System.currentTimeMillis();
            //设置连续两次发送文本的最小时间间隔（毫秒），如果当前调用send时距离上次调用时间小于此值，则会阻塞并等待直到满足条件再发送文本
            synthesizer.setMinSendIntervalMS(100);
            boolean firstChunk = true;

//            emitter.send(SseEmitter.event().name("newAudio").data("Audio generated Start."));
//            do {
//                String chunk = concurrentAnswerQueue.poll();
//
//                if (chunk != null && !chunk.isEmpty()) {
//
//                    if (firstChunk) {
//                        firstChunk = false;
//                        FlowingSpeechSynthesizerService.startTime = System.currentTimeMillis();
//                    }
//
//                    synthesizer.send(chunk);
//                }
//                Thread.sleep(100);
//
//            } while (!isProcessFinished.get() || !concurrentAnswerQueue.isEmpty());
//
//            //通知服务端流入文本数据发送完毕，阻塞等待服务端处理完成。
//            //如果连续超过10s没有返回任何音频数据或json数据包，则会抛出异常。
//            //可以通过使用synthesizer.reSetCountdown()重置10s计时器。
//            synthesizer.stop();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭连接
//            if (null != synthesizer) {
//                synthesizer.close();
//            }
        }

        return synthesizer;
    }


}
