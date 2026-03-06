package com.tracker.repository;

import com.tracker.entity.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Long> {
    List<MedicationSchedule> findByPatientProfileId(Long patientProfileId);

    List<MedicationSchedule> findByPatientProfileIdAndScheduledTimeBetween(Long patientProfileId, LocalDateTime start,
            LocalDateTime end);

    List<MedicationSchedule> findByStatusAndScheduledTimeBefore(MedicationSchedule.ScheduleStatus status,
            LocalDateTime time);

    List<MedicationSchedule> findByPatientProfileIdAndStatus(Long patientProfileId,
            MedicationSchedule.ScheduleStatus status);
}
