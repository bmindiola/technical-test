package com.example.worker.service.impl;

import com.example.worker.model.Order;
import com.example.worker.service.RetryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RetryServiceImpl implements RetryService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RetryServiceImpl.class);

    @Override
    public Mono<Void> handleRetry(Order order) {
        String redisKey = "order_retry_" + order.getOrderId();
        return reactiveRedisTemplate.opsForValue()
                .get(redisKey)
                .defaultIfEmpty(0)
                .flatMap(retryCount -> {
                    if ((Integer) retryCount < 5) {
                        return reactiveRedisTemplate.opsForValue().set(redisKey,(Integer) retryCount + 1).then();
                    } else {
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Boolean> acquireLock(String orderId) {
        String lockKey = "order_lock_" + orderId;
        logger.info("Trying to acquire lock for orderId: {}", orderId);
        logger.info("LockKey: {}", lockKey);
        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1")
                .flatMap(success -> reactiveRedisTemplate.expire(lockKey, Duration.ofMinutes(10)).thenReturn(success));
    }

    @Override
    public Mono<Void> releaseLock(String orderId) {
        String lockKey = "order_lock_" + orderId;
        return reactiveRedisTemplate.delete(lockKey).then();
    }
}
