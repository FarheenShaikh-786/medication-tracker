package com.tracker.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PatientDashboardResponse {
    private Map<String, Double> adherenceGraphData;
    private int totalMedicinesTaken;
    private int totalMedicinesMissed;
}
