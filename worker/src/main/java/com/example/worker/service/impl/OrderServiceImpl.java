package com.example.worker.service.impl;

import com.example.worker.exception.OrderProcessingException;
import com.example.worker.model.Order;
import com.example.worker.repository.OrderRepository;
import com.example.worker.service.EnrichmentService;
import com.example.worker.service.OrderService;
import com.example.worker.service.RetryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final EnrichmentService enrichmentService;
    private final OrderRepository orderRepository;
    private final RetryService retryService;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    public void processOrder(Order order) {
        retryService.acquireLock(order.getOrderId())
                .flatMap(isLocked -> {
                    if (!isLocked) {
                        return Mono.error(new OrderProcessingException("Order already locked."));
                    }

                    return enrichmentService.enrichCustomer(order.getCustomerId())
                            .doOnNext(customer -> {
                                logger.info("Customer enriched: {}", customer);
                                if (!"active".equals(customer.getStatus())) {
                                    throw new OrderProcessingException("Customer is not active.");
                                }
                            })

                            .zipWith(Flux.fromIterable(order.getProducts())
                                    .flatMap(product -> enrichmentService.enrichProduct(product.getProductId())
                                            .doOnNext(enrichedProduct -> {
                                                logger.info("Product enriched: {}", enrichedProduct);
                                                if (enrichedProduct == null || enrichedProduct.getName() == null) {
                                                    throw new OrderProcessingException("Product not found in catalog.");
                                                }
                                            }))
                                    .collectList()
                            );
                })
                .flatMap(tuple -> {
                    order.setProducts(tuple.getT2());
                    logger.info("Saving order: {}", order);
                    return orderRepository.save(order)
                            .doOnSuccess(savedOrder -> logger.info("Order saved successfully: {}", savedOrder))
                            .doOnError(error -> logger.error("Error saving order: {}", error.getMessage()));
                })
                .flatMap(savedOrder -> retryService.releaseLock(order.getOrderId()))
                .onErrorResume(error -> retryService.handleRetry(order).then(Mono.error(error)))
                .doOnSuccess(success -> logger.info("Order processed successfully: {}", order.getOrderId()))
                .doOnError(error -> logger.error("Error processing order: {}", error.getMessage()))
                .subscribe();
    }
}