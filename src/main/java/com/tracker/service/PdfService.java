package com.tracker.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tracker.entity.Prescription;
import com.tracker.entity.PrescriptionItem;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PdfService {

    private static final String PDF_DIR = "generated_pdfs/";

    public String generatePrescriptionPdf(Prescription prescription) {
        Document document = new Document();
        String fileName = "prescription_" + UUID.randomUUID().toString() + ".pdf";
        String filePath = PDF_DIR + fileName;

        try {
            File dir = new File(PDF_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE);
            Paragraph title = new Paragraph("Medical Prescription", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Details
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

            document.add(new Paragraph("Prescription ID: " + prescription.getId(), regularFont));
            document.add(new Paragraph(
                    "Date: " + prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    regularFont));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Doctor: Dr. " + prescription.getDoctorProfile().getFirstName() + " "
                    + prescription.getDoctorProfile().getLastName(), boldFont));
            document.add(new Paragraph("Specialization: " + prescription.getDoctorProfile().getSpecialization(),
                    regularFont));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Patient: " + prescription.getPatientProfile().getFirstName() + " "
                    + prescription.getPatientProfile().getLastName(), boldFont));
            document.add(new Paragraph("Diagnosis: " + prescription.getDiagnosis(), regularFont));
            if (prescription.getValidUntil() != null) {
                document.add(new Paragraph("Valid Until: " + prescription.getValidUntil(), regularFont));
            }

            document.add(Chunk.NEWLINE);

            // Table for Medicines
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            String[] headers = { "Medicine", "Dosage", "Frequency", "Duration", "Instructions" };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (PrescriptionItem item : prescription.getPrescriptionItems()) {
                table.addCell(item.getMedicine().getName());
                table.addCell(item.getDosage());
                table.addCell(item.getFrequency());
                table.addCell(String.valueOf(item.getDurationDays()) + " days");
                table.addCell(item.getInstructions());
            }

            document.add(table);
            document.close();

            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
