package com.example.worker.service.impl;

import com.example.worker.consumer.OrderConsumer;
import com.example.worker.model.Customer;
import com.example.worker.model.Product;
import com.example.worker.service.EnrichmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;


@Service
public class EnrichmentServiceImpl implements EnrichmentService {

    private final WebClient webClient;

    @Value("${spring.enrichment.product-api.base-url}")
    private String productApiBaseUrl;

    @Value("${spring.enrichment.customer-api.base-url}")
    private String customerApiBaseUrl;

    public EnrichmentServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    private static final Logger logger = LoggerFactory.getLogger(EnrichmentServiceImpl.class);

    @Override
    public Mono<Product> enrichProduct(String productId) {
        logger.info("Calling product enrichment service for productId: {}", productId);
        logger.info("Product baseUrl: {}", productApiBaseUrl);

        return webClient.get()
                .uri(productApiBaseUrl + "/product?id=" + productId)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnNext(product -> logger.info("Product enriched: {}", product))
                .doOnError(error -> logger.error("Error calling product enrichment service: {}", error.getMessage()));
    }

    @Override
    public Mono<Customer> enrichCustomer(String customerId) {
        logger.info("Calling customer enrichment service for customerId: {}", customerId);
        logger.info("Customer baseUrl: {}", customerApiBaseUrl);

        return webClient.get()
                .uri(customerApiBaseUrl + "/customer?id=" + customerId)  // Construir la URL completa
                .retrieve()
                .bodyToMono(Customer.class)
                .doOnNext(customer -> logger.info("Customer enriched: {}", customer))
                .doOnError(error -> logger.error("Error calling customer enrichment service: {}", error.getMessage()));
    }
}


