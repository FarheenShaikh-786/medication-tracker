package com.tracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class PrescriptionRequest {
    private Long patientUserId;
    private String diagnosis;
    private String validUntil; // e.g. "2024-12-31"
    private List<PrescriptionItemRequest> items;
}
