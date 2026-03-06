package com.tracker.controller;

import com.tracker.dto.DrugInfoResponse;
import com.tracker.dto.InventoryRequest;
import com.tracker.dto.InventoryResponse;
import com.tracker.dto.MessageResponse;
import com.tracker.service.DrugApiService;
import com.tracker.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final DrugApiService drugApiService;
    private final com.tracker.repository.UserRepository userRepository;

    private Long getUserId(Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<InventoryResponse> addOrUpdateMedicineStock(
            @RequestBody InventoryRequest request, Authentication auth) {
        return ResponseEntity.ok(inventoryService.addOrUpdateMedicineStock(getUserId(auth), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<List<InventoryResponse>> getInventory(Authentication auth) {
        return ResponseEntity.ok(inventoryService.getPharmacistInventory(getUserId(auth)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<MessageResponse> deleteInventoryItem(
            @PathVariable Long id, Authentication auth) {
        inventoryService.deleteInventoryItem(getUserId(auth), id);
        return ResponseEntity.ok(new MessageResponse("Inventory item deleted successfully"));
    }

    @GetMapping("/drug-info/{medicineName}")
    @PreAuthorize("hasRole('PHARMACIST') or hasRole('DOCTOR')")
    public ResponseEntity<DrugInfoResponse> getDrugInfo(@PathVariable String medicineName) {
        DrugInfoResponse info = drugApiService.getDrugInfo(medicineName);
        if (info != null) {
            return ResponseEntity.ok(info);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
