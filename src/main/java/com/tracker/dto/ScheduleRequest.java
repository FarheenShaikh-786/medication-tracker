package com.tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleRequest {
    private Long prescriptionItemId;
    private LocalDateTime scheduledTime;
    private String recurrence; // e.g. "DAILY", "WEEKLY", "CUSTOM"
    private int daysToSchedule; // How many days ahead to generate schedules
}
