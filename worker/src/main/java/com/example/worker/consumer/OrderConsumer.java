package com.example.worker.consumer;

import com.example.worker.model.Order;
import com.example.worker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderService orderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(topics = "orders", groupId = "order-consumer-group")
    public void listen(Order order) {
        logger.info("Message Received: {}", order.toString());
        orderService.processOrder(order);
    }
}
