package com.atguigu.gmall1129.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @param
 * @return
 */

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:disabled}")
    String redisHost;

    @Value("${spring.redis.port:0}")
    int redisPort;

    @Bean
    public RedisUtil getRedisUtil(){
        if(redisHost.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil =new RedisUtil();
        redisUtil.initJedisPool(redisHost,redisPort);
        return redisUtil;
    }
}
