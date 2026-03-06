package com.tracker.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.tracker.dto.PatientDashboardResponse;
import com.tracker.entity.*;
import com.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MedicationScheduleRepository scheduleRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final PatientProfileRepository patientProfileRepository;

    public PatientDashboardResponse getPatientDashboard(Long patientUserId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        List<MedicationSchedule> schedules = scheduleRepository.findByPatientProfileId(patient.getId());

        PatientDashboardResponse response = new PatientDashboardResponse();
        int taken = 0;
        int missed = 0;
        Map<String, Double> adherenceGraphData = new HashMap<>(); // Mock graph data distribution by month logic
        adherenceGraphData.put("Current Month", 100.0);

        for (MedicationSchedule schedule : schedules) {
            if (schedule.getStatus() == MedicationSchedule.ScheduleStatus.TAKEN)
                taken++;
            if (schedule.getStatus() == MedicationSchedule.ScheduleStatus.MISSED)
                missed++;
        }

        if (taken + missed > 0) {
            adherenceGraphData.put("Current Month", ((double) taken / (taken + missed)) * 100);
        }

        response.setAdherenceGraphData(adherenceGraphData);
        response.setTotalMedicinesTaken(taken);
        response.setTotalMedicinesMissed(missed);

        return response;
    }

    public Map<String, Object> getDoctorDashboard(Long doctorUserId) {
        // Find total prescriptions by doctor
        long count = prescriptionRepository.findAll().stream()
                .filter(p -> p.getDoctorProfile().getUser().getId().equals(doctorUserId))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("totalPrescriptionsIssued", count);
        response.put("averagePatientAdherence", 85.5); // Mocked aggregation across all patients
        return response;
    }

    public Map<String, Object> getPharmacistDashboard(Long pharmacistUserId) {
        // Usually filtered by profile, executing broad strokes for demo
        long lowStock = inventoryRepository.findByStockQuantityLessThanEqual(50).size();
        long expired = inventoryRepository.findByExpiryDateBefore(java.time.LocalDate.now()).size();

        Map<String, Object> response = new HashMap<>();
        response.put("lowStockCount", lowStock);
        response.put("expiredMedicinesCount", expired);

        Map<String, Integer> topSelling = new HashMap<>();
        topSelling.put("Paracetamol", 150);
        topSelling.put("Amoxicillin", 90);
        response.put("topSellingMedicines", topSelling);

        return response;
    }

    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", userRepository.count());
        response.put("totalPrescriptions", prescriptionRepository.count());
        response.put("totalAlerts", auditLogRepository.count()); // Simple alias for now
        return response;
    }

    public byte[] exportSystemReportCsv() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Metric,Value");
        pw.println("Total Users," + userRepository.count());
        pw.println("Total Prescriptions," + prescriptionRepository.count());
        pw.println("Total Audit Logs," + auditLogRepository.count());
        return sw.toString().getBytes();
    }

    public byte[] exportSystemReportPdf() {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("System Analytics Report"));
            document.add(new Paragraph("Total Users: " + userRepository.count()));
            document.add(new Paragraph("Total Prescriptions: " + prescriptionRepository.count()));
            document.add(new Paragraph("Total Audit Logs: " + auditLogRepository.count()));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Could not generate PDF Report");
        }
    }
}
