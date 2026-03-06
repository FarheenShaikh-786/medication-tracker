package com.tracker.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryRequest {
    private String medicineName;
    private String batchNumber;
    private int stockQuantity;
    private LocalDate expiryDate;
    private int lowStockThreshold; // e.g. 50
    // Optional details for newly added medicines
    private String manufacturer;
    private String description;
}
