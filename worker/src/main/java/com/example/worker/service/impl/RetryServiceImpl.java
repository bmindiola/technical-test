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
        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1")
                .flatMap(success -> {
                    if (success) {
                        logger.info("Lock acquired for order {}", orderId);
                        return reactiveRedisTemplate.expire(lockKey, Duration.ofMinutes(10)).thenReturn(true);
                    } else {
                        logger.warn("Failed to acquire lock for order {}", orderId);
                        return Mono.just(false);
                    }
                });
    }

    @Override
    public Mono<Void> releaseLock(String orderId) {
        String lockKey = "order_lock_" + orderId;
        return reactiveRedisTemplate.delete(lockKey)
                .doOnSuccess(deleted -> {
                    if (deleted > 0) {
                        logger.info("Lock released for order {}", orderId);
                    } else {
                        logger.warn("Failed to release lock for order {}", orderId);
                    }
                }).then();
    }
}
