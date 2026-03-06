package com.tracker.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryResponse {
    private Long id;
    private String medicineName;
    private String batchNumber;
    private int stockQuantity;
    private LocalDate expiryDate;
}
