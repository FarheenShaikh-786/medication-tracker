package com.tracker.controller;

import com.tracker.dto.AdherenceReport;
import com.tracker.dto.ScheduleRequest;
import com.tracker.dto.ScheduleResponse;
import com.tracker.security.CustomUserDetailsService;
import com.tracker.service.MedicationTrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracker")
@RequiredArgsConstructor
public class TrackerController {

    private final MedicationTrackerService trackerService;
    private final com.tracker.repository.UserRepository userRepository;

    private Long getUserId(Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ScheduleResponse>> createSchedule(
            @RequestBody ScheduleRequest request, Authentication auth) {
        return ResponseEntity.ok(trackerService.generateSchedule(getUserId(auth), request));
    }

    @PatchMapping("/schedule/{id}/status")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ScheduleResponse> markDoseStatus(
            @PathVariable Long id, @RequestBody Map<String, String> payload, Authentication auth) {
        return ResponseEntity.ok(trackerService.markDose(getUserId(auth), id, payload.get("status")));
    }

    @PatchMapping("/schedule/{id}/snooze")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ScheduleResponse> snoozeReminder(
            @PathVariable Long id, @RequestParam int minutes, Authentication auth) {
        return ResponseEntity.ok(trackerService.snoozeReminder(getUserId(auth), id, minutes));
    }

    @GetMapping("/adherence")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<AdherenceReport> getAdherence(Authentication auth) {
        return ResponseEntity.ok(trackerService.calculateAdherence(getUserId(auth)));
    }
}
