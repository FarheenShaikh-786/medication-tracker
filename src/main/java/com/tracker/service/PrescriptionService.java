package com.tracker.service;

import com.tracker.dto.PrescriptionItemRequest;
import com.tracker.dto.PrescriptionRequest;
import com.tracker.dto.PrescriptionResponse;
import com.tracker.entity.*;
import com.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AuditLogRepository auditLogRepository;
    private final PdfService pdfService;

    @Transactional
    public PrescriptionResponse createPrescription(Long doctorUserId, PrescriptionRequest request) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        PatientProfile patient = patientProfileRepository.findByUserId(request.getPatientUserId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Prescription prescription = new Prescription();
        prescription.setDoctorProfile(doctor);
        prescription.setPatientProfile(patient);
        prescription.setDiagnosis(request.getDiagnosis());
        if (request.getValidUntil() != null && !request.getValidUntil().isEmpty()) {
            prescription.setValidUntil(LocalDate.parse(request.getValidUntil()));
        }

        for (PrescriptionItemRequest itemReq : request.getItems()) {
            Medicine medicine = medicineRepository.findByNameIgnoreCase(itemReq.getMedicineName())
                    .orElseGet(() -> {
                        Medicine newMed = new Medicine();
                        newMed.setName(itemReq.getMedicineName());
                        return medicineRepository.save(newMed);
                    });

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setMedicine(medicine);
            item.setDosage(itemReq.getDosage());
            item.setFrequency(itemReq.getFrequency());
            item.setDurationDays(itemReq.getDurationDays());
            item.setInstructions(itemReq.getInstructions());

            prescription.getPrescriptionItems().add(item);
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        // Generate PDF
        String pdfPath = pdfService.generatePrescriptionPdf(savedPrescription);
        savedPrescription.setPdfPath(pdfPath);
        savedPrescription = prescriptionRepository.save(savedPrescription);

        logAuditAction(doctorUserId, "CREATED", "Prescription", savedPrescription.getId());

        return mapToResponse(savedPrescription);
    }

    @Transactional
    public PrescriptionResponse renewPrescription(Long doctorUserId, Long prescriptionId, PrescriptionRequest request) {
        Prescription existing = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        // Logging version history
        logAuditAction(doctorUserId, "RENEWED_FROM_" + existing.getId(), "Prescription", existing.getId());

        // Essentially a create but keeping the logical link via audit logs
        return createPrescription(doctorUserId, request);
    }

    public List<PrescriptionResponse> getPatientPrescriptions(Long patientUserId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        return prescriptionRepository.findByPatientProfileId(patient.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponse> getDoctorPrescriptions(Long doctorUserId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        return prescriptionRepository.findByDoctorProfileId(doctor.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void logAuditAction(Long userId, String action, String entityName, Long entityId) {
        AuditLog auditLog = new AuditLog();
        User user = new User();
        user.setId(userId);
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLogRepository.save(auditLog);
    }

    private PrescriptionResponse mapToResponse(Prescription p) {
        PrescriptionResponse resp = new PrescriptionResponse();
        resp.setId(p.getId());
        resp.setDoctorName(p.getDoctorProfile().getFirstName() + " " + p.getDoctorProfile().getLastName());
        resp.setPatientName(p.getPatientProfile().getFirstName() + " " + p.getPatientProfile().getLastName());
        resp.setDiagnosis(p.getDiagnosis());
        resp.setValidUntil(p.getValidUntil());
        resp.setPdfPath(p.getPdfPath());
        resp.setCreatedAt(p.getCreatedAt());
        resp.setUpdatedAt(p.getUpdatedAt());

        List<PrescriptionResponse.PrescriptionItemResponse> items = new ArrayList<>();
        if (p.getPrescriptionItems() != null) {
            for (PrescriptionItem pi : p.getPrescriptionItems()) {
                PrescriptionResponse.PrescriptionItemResponse piResp = new PrescriptionResponse.PrescriptionItemResponse();
                piResp.setId(pi.getId());
                piResp.setMedicineName(pi.getMedicine().getName());
                piResp.setDosage(pi.getDosage());
                piResp.setFrequency(pi.getFrequency());
                piResp.setDurationDays(pi.getDurationDays());
                piResp.setInstructions(pi.getInstructions());
                items.add(piResp);
            }
        }
        resp.setItems(items);
        return resp;
    }
}
