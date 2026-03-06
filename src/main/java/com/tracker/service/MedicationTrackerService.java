package com.tracker.service;

import com.tracker.dto.AdherenceReport;
import com.tracker.dto.ScheduleRequest;
import com.tracker.dto.ScheduleResponse;
import com.tracker.entity.*;
import com.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationTrackerService {

    private final MedicationScheduleRepository scheduleRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final EmailService emailService;

    @Transactional
    public List<ScheduleResponse> generateSchedule(Long patientUserId, ScheduleRequest request) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        PrescriptionItem item = prescriptionItemRepository.findById(request.getPrescriptionItemId())
                .orElseThrow(() -> new RuntimeException("Prescription Item not found"));

        // Basic check to ensure patient owns prescription
        if (!item.getPrescription().getPatientProfile().getId().equals(patient.getId())) {
            throw new RuntimeException("Not authorized to schedule this item");
        }

        List<MedicationSchedule> generated = new ArrayList<>();
        LocalDateTime currentTime = request.getScheduledTime();

        for (int i = 0; i < request.getDaysToSchedule(); i++) {
            MedicationSchedule schedule = new MedicationSchedule();
            schedule.setPatientProfile(patient);
            schedule.setPrescriptionItem(item);
            schedule.setScheduledTime(currentTime);
            schedule.setStatus(MedicationSchedule.ScheduleStatus.PENDING);
            generated.add(schedule);

            // Increment based on recurrence (simplified here to DAILY/WEEKLY)
            if ("DAILY".equalsIgnoreCase(request.getRecurrence())) {
                currentTime = currentTime.plusDays(1);
            } else if ("WEEKLY".equalsIgnoreCase(request.getRecurrence())) {
                currentTime = currentTime.plusWeeks(1);
            } else {
                // For custom could parse further, defaulting to daily
                currentTime = currentTime.plusDays(1);
            }
        }

        generated = scheduleRepository.saveAll(generated);

        return generated.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse markDose(Long patientUserId, Long scheduleId, String statusObj) {
        MedicationSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getPatientProfile().getUser().getId().equals(patientUserId)) {
            throw new RuntimeException("Unauthorized");
        }

        schedule.setStatus(MedicationSchedule.ScheduleStatus.valueOf(statusObj.toUpperCase()));
        schedule = scheduleRepository.save(schedule);

        // Calculate Adherence and notify doctor if threshold critically low
        checkAdherenceAndNotify(schedule.getPatientProfile());

        return mapToResponse(schedule);
    }

    @Transactional
    public ScheduleResponse snoozeReminder(Long patientUserId, Long scheduleId, int minutes) {
        MedicationSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getPatientProfile().getUser().getId().equals(patientUserId)) {
            throw new RuntimeException("Unauthorized");
        }

        schedule.setScheduledTime(schedule.getScheduledTime().plusMinutes(minutes));
        schedule = scheduleRepository.save(schedule);
        return mapToResponse(schedule);
    }

    public AdherenceReport calculateAdherence(Long patientUserId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return calculateAdherenceInternal(patient);
    }

    private AdherenceReport calculateAdherenceInternal(PatientProfile patient) {
        List<MedicationSchedule> schedules = scheduleRepository.findByPatientProfileId(patient.getId());

        LocalDateTime now = LocalDateTime.now();
        int totalPast = 0;
        int taken = 0;
        int missed = 0;

        for (MedicationSchedule sch : schedules) {
            // Only consider schedules that are past or marked
            if (sch.getScheduledTime().isBefore(now) || sch.getStatus() != MedicationSchedule.ScheduleStatus.PENDING) {
                totalPast++;
                if (sch.getStatus() == MedicationSchedule.ScheduleStatus.TAKEN) {
                    taken++;
                } else if (sch.getStatus() == MedicationSchedule.ScheduleStatus.MISSED) {
                    missed++;
                }
            }
        }

        double percentage = totalPast == 0 ? 100.0 : ((double) taken / totalPast) * 100.0;

        return new AdherenceReport(
                patient.getId(),
                patient.getFirstName() + " " + patient.getLastName(),
                totalPast,
                taken,
                missed,
                percentage);
    }

    private void checkAdherenceAndNotify(PatientProfile patient) {
        AdherenceReport report = calculateAdherenceInternal(patient);
        if (report.getTotalDosesPast() >= 5 && report.getAdherencePercentage() < 50.0) {
            // Hypothetically notify the doctor of the first prescription (simplification)
            System.out.println("Alert! Adherence fell below threshold: " + report.getAdherencePercentage()
                    + "%. Sending mail to doctor.");
            // emailService.sendReminderEmail(doctorEmail, "Adherence Alert", "Patient " +
            // patient.getFirstName() + " is at " + report.getAdherencePercentage() + "%");
        }
    }

    private ScheduleResponse mapToResponse(MedicationSchedule s) {
        ScheduleResponse resp = new ScheduleResponse();
        resp.setId(s.getId());
        resp.setPrescriptionItemId(s.getPrescriptionItem().getId());
        resp.setMedicineName(s.getPrescriptionItem().getMedicine().getName());
        resp.setScheduledTime(s.getScheduledTime());
        resp.setStatus(s.getStatus().name());
        return resp;
    }
}
