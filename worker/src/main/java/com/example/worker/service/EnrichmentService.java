package com.example.worker.service;

import com.example.worker.model.Customer;
import com.example.worker.model.Product;
import reactor.core.publisher.Mono;

public interface EnrichmentService {
    Mono<Product> enrichProduct(String productId);
    Mono<Customer> enrichCustomer(String customerId);
}

