package com.tracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleResponse {
    private Long id;
    private Long prescriptionItemId;
    private String medicineName;
    private LocalDateTime scheduledTime;
    private String status;
}
