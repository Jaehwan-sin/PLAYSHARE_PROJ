package com.tech.spotify.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void 레디스테스트() {
        // given
        String key = "testKey";
        String value = "testValue";

        // when
        redisService.save(key, value);
        String result = redisService.get(key);

        // then
        assertEquals(value, result, "Redis 정상 작동");
        System.out.println("result = " + result);
    }

}