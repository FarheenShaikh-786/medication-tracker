package com.tracker.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionResponse {
    private Long id;
    private String doctorName;
    private String patientName;
    private String diagnosis;
    private LocalDate validUntil;
    private String pdfPath;
    private List<PrescriptionItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class PrescriptionItemResponse {
        private Long id;
        private String medicineName;
        private String dosage;
        private String frequency;
        private int durationDays;
        private String instructions;
    }
}
