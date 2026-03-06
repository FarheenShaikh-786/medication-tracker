package com.tracker.controller;

import com.tracker.dto.PrescriptionRequest;
import com.tracker.dto.PrescriptionResponse;
import com.tracker.security.CustomUserDetailsService;
import com.tracker.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final CustomUserDetailsService userDetailsService;
    private final com.tracker.repository.UserRepository userRepository; // needed to get user IDs easily

    private Long getUserId(Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @RequestBody PrescriptionRequest request, Authentication auth) {
        return ResponseEntity.ok(prescriptionService.createPrescription(getUserId(auth), request));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> renewPrescription(
            @PathVariable Long id, @RequestBody PrescriptionRequest request, Authentication auth) {
        return ResponseEntity.ok(prescriptionService.renewPrescription(getUserId(auth), id, request));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PrescriptionResponse>> getPatientPrescriptions(Authentication auth) {
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(getUserId(auth)));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PrescriptionResponse>> getDoctorPrescriptions(Authentication auth) {
        return ResponseEntity.ok(prescriptionService.getDoctorPrescriptions(getUserId(auth)));
    }

    @GetMapping("/download/{fileName:.+}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<Resource> downloadPrescriptionPdf(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("generated_pdfs").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
