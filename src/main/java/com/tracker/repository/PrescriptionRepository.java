package com.tracker.repository;

import com.tracker.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientProfileId(Long patientProfileId);

    List<Prescription> findByDoctorProfileId(Long doctorProfileId);
}
