package com.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdherenceReport {
    private Long patientId;
    private String patientName;
    private int totalDosesPast;
    private int dosesTaken;
    private int dosesMissed;
    private double adherencePercentage;
}
