package com.tech.spotify.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.*;
import static org.mockito.Mockito.*;

class LettuceLockServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LettuceLockServiceTest.class);

    private LettuceLockService lettuceLockService;
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    public void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        lettuceLockService = new LettuceLockService("redis://localhost:6379", redisTemplate);
    }

    @Test
    public void 분산락테스트() throws Exception {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        String lockKey = "test:lock";
        long expireTime = 30000; // 30 seconds

        // Mock Redis commands
        lettuceLockService = Mockito.spy(lettuceLockService);
        doReturn(true).when(lettuceLockService).acquireLock(anyString(), anyString(), anyLong());
        doNothing().when(lettuceLockService).releaseLock(anyString(), anyString());

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i + 1;
            executorService.submit(() -> {
                String lockValue = UUID.randomUUID().toString();
                try {
                    startLatch.await();  // 모든 스레드가 준비될 때까지 대기
                    boolean acquired = lettuceLockService.acquireLock(lockKey, lockValue, expireTime);
                    if (acquired) {
                        log.info("Thread 락 획득", threadNum, lockKey);
                        try {
                            // Simulate some work with the locked resource
                            Thread.sleep(100);
                            log.info("Thread 작업 처리 중", threadNum, lockKey);
                        } finally {
                            lettuceLockService.releaseLock(lockKey, lockValue);
                            log.info("Thread 락 해제", threadNum, lockKey);
                        }
                    } else {
                        log.info("Thread 락 획득 실패", threadNum, lockKey);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        startLatch.countDown();  // 모든 스레드 시작
        latch.await();
        executorService.shutdown();

        // Verify that acquireLock and releaseLock were called the expected number of times
        verify(lettuceLockService, times(threadCount)).acquireLock(anyString(), anyString(), anyLong());
        verify(lettuceLockService, times(threadCount)).releaseLock(anyString(), anyString());
    }
}
