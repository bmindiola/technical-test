package com.example.worker.service;

import com.example.worker.model.Order;
import reactor.core.publisher.Mono;

public interface RetryService {
    Mono<Void> handleRetry(Order order);
    Mono<Boolean> acquireLock(String orderId);
    Mono<Void> releaseLock(String orderId);
}

