package com.tracker.dto;

import lombok.Data;

@Data
public class PrescriptionItemRequest {
    private String medicineName;
    private String dosage;
    private String frequency;
    private int durationDays;
    private String instructions;
}
