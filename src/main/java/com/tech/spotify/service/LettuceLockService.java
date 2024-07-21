package com.tech.spotify.service;

import io.lettuce.core.RedisClient;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.data.redis.core.RedisTemplate;

public class LettuceLockService {
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;
    private final RedisTemplate<String, String> redisTemplate;

    public LettuceLockService(String redisUri, RedisTemplate<String, String> redisTemplate) {
        this.redisClient = RedisClient.create(redisUri);
        this.redisTemplate = redisTemplate;
        this.connection = redisClient.connect();
        this.commands = connection.sync();
    }

    // 분산 락 획득 메서드
    public boolean acquireLock(String lockKey, String lockValue, long expireTime) {
        String result = commands.set(lockKey, lockValue, SetArgs.Builder.nx().px(expireTime));
        return "OK".equals(result);
    }

    // 락 해제 메서드
    public void releaseLock(String lockKey, String lockValue) {
        String currentValue = commands.get(lockKey);
        if (lockValue.equals(currentValue)) {
            commands.del(lockKey);
        }
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}
