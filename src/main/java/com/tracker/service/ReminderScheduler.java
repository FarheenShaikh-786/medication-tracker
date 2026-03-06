package com.tracker.service;

import com.tracker.entity.MedicationSchedule;
import com.tracker.entity.ReminderLog;
import com.tracker.repository.MedicationScheduleRepository;
import com.tracker.repository.ReminderLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final MedicationScheduleRepository scheduleRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final EmailService emailService;

    // Run every minute to check for schedules due in the next 15 minutes that
    // haven't been notified
    @Scheduled(cron = "0 * * * * *")
    public void sendUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(15);

        List<MedicationSchedule> dueSchedules = scheduleRepository
                .findByStatusAndScheduledTimeBefore(MedicationSchedule.ScheduleStatus.PENDING, threshold);

        for (MedicationSchedule schedule : dueSchedules) {
            // Check if reminder was already sent
            List<ReminderLog> existingLogs = reminderLogRepository.findByMedicationScheduleId(schedule.getId());
            boolean alreadySent = existingLogs.stream()
                    .anyMatch(log -> log.getStatus() == ReminderLog.ReminderStatus.SENT);

            if (!alreadySent) {
                String toEmail = schedule.getPatientProfile().getUser().getEmail();
                String subject = "Medication Reminder: " + schedule.getPrescriptionItem().getMedicine().getName();
                String body = "Dear " + schedule.getPatientProfile().getFirstName() + ",\n\n" +
                        "This is a reminder to take your medication: "
                        + schedule.getPrescriptionItem().getMedicine().getName() +
                        " at " + schedule.getScheduledTime() + ".\n\n" +
                        "Dosage: " + schedule.getPrescriptionItem().getDosage() + "\n" +
                        "Instructions: " + schedule.getPrescriptionItem().getInstructions() + "\n\n" +
                        "Please mark it as taken in the tracker app once you consume it.\n\n" +
                        "Stay Healthy,\nMediTracker Team";

                emailService.sendReminderEmail(toEmail, subject, body);

                ReminderLog log = new ReminderLog();
                log.setMedicationSchedule(schedule);
                log.setSentAt(LocalDateTime.now());
                log.setStatus(ReminderLog.ReminderStatus.SENT);
                reminderLogRepository.save(log);
            }
        }
    }
}
