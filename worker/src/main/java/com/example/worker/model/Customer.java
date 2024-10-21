package com.example.worker.model;

import lombok.Data;

@Data
public class Customer {
    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String status;
}
