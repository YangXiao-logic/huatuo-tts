package com.yx.huatuotts.service;


import cn.hutool.core.date.DateUtil;
import com.alibaba.nls.client.AccessToken;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    @Value("${aliyun.ak_id}")
    private String AKID;

    @Value("${aliyun.ak_secret}")
    private String AKSECRET;

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private long tokenExpirationTime;
    private String token;

    @PostConstruct
    public void init() {
        // 在这里执行初始化代码
        try {
            this.getToken();
        } catch (IOException e) {
            logger.error("Failed to get Token: {}", e.getMessage());
        }
    }



    public String getToken() throws IOException {
        // 如果token过期或者token为空，重新获取token
        AccessToken accessToken = new AccessToken(AKID, AKSECRET);

        accessToken.apply();
        token = accessToken.getToken();
        tokenExpirationTime = accessToken.getExpireTime();
        logger.info("Getting New Token");
        logger.info("Token: {}", token);
        logger.info("Token Expiration Time: {}", printHumanReadableDate(tokenExpirationTime));
//        if (token == null || isTokenExpired()) {
//
//        }
        return token;
    }

    public boolean isTokenExpired() {
        // 提前一分钟来判断token是否过期
        Instant currentTime = Instant.now();
        Instant expirationMinusOneMinute = Instant.ofEpochSecond(tokenExpirationTime).minus(1, ChronoUnit.MINUTES);
        return currentTime.isAfter(expirationMinusOneMinute);
    }

    public String printHumanReadableDate(long timestamp) {
        // 将Unix时间戳转换为Date对象
        Date date = DateUtil.date(timestamp * 1000); // 注意时间戳需要乘以1000转换为毫秒
        // 格式化日期为人类可读的格式，例如: yyyy-MM-dd HH:mm:ss
        String formattedDate = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
        return "Human-readable date: " + formattedDate;
    }


}
