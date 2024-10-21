package com.example.worker.model;

import lombok.Data;

@Data
public class Product {
    private String productId;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
}
