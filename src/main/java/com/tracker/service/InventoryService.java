package com.tracker.service;

import com.tracker.dto.InventoryRequest;
import com.tracker.dto.InventoryResponse;
import com.tracker.entity.*;
import com.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;
    private final PharmacistProfileRepository pharmacistProfileRepository;
    private final AuditLogRepository auditLogRepository;

    // In a real application, these thresholds could be configured per pharmacy or
    // medicine.
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 50;

    @Transactional
    public InventoryResponse addOrUpdateMedicineStock(Long pharmacistUserId, InventoryRequest request) {
        PharmacistProfile pharmacist = pharmacistProfileRepository.findByUserId(pharmacistUserId)
                .orElseThrow(() -> new RuntimeException("Pharmacist profile not found"));

        Medicine medicine = medicineRepository.findByNameIgnoreCase(request.getMedicineName())
                .orElseGet(() -> {
                    Medicine newMed = new Medicine();
                    newMed.setName(request.getMedicineName());
                    newMed.setManufacturer(request.getManufacturer());
                    newMed.setDescription(request.getDescription());
                    return medicineRepository.save(newMed);
                });

        Optional<Inventory> optInventory = inventoryRepository.findByPharmacistProfileIdAndMedicineIdAndBatchNumber(
                pharmacist.getId(), medicine.getId(), request.getBatchNumber());

        Inventory inventory;
        if (optInventory.isPresent()) {
            inventory = optInventory.get();
            inventory.setStockQuantity(inventory.getStockQuantity() + request.getStockQuantity());
            inventory.setExpiryDate(request.getExpiryDate());
            logAuditAction(pharmacistUserId, "UPDATED_STOCK_TO_" + inventory.getStockQuantity(), "Inventory",
                    inventory.getId());
        } else {
            inventory = new Inventory();
            inventory.setPharmacistProfile(pharmacist);
            inventory.setMedicine(medicine);
            inventory.setBatchNumber(request.getBatchNumber());
            inventory.setStockQuantity(request.getStockQuantity());
            inventory.setExpiryDate(request.getExpiryDate());
            inventory = inventoryRepository.save(inventory);
            logAuditAction(pharmacistUserId, "ADDED_STOCK_" + inventory.getStockQuantity(), "Inventory",
                    inventory.getId());
        }

        return mapToResponse(inventoryRepository.save(inventory));
    }

    public List<InventoryResponse> getPharmacistInventory(Long pharmacistUserId) {
        PharmacistProfile pharmacist = pharmacistProfileRepository.findByUserId(pharmacistUserId)
                .orElseThrow(() -> new RuntimeException("Pharmacist profile not found"));

        return inventoryRepository.findByPharmacistProfileId(pharmacist.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteInventoryItem(Long pharmacistUserId, Long inventoryId) {
        PharmacistProfile pharmacist = pharmacistProfileRepository.findByUserId(pharmacistUserId)
                .orElseThrow(() -> new RuntimeException("Pharmacist profile not found"));

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (!inventory.getPharmacistProfile().getId().equals(pharmacist.getId())) {
            throw new RuntimeException("Unauthorized to delete this inventory");
        }

        inventoryRepository.delete(inventory);
        logAuditAction(pharmacistUserId, "DELETED", "Inventory", inventoryId);
    }

    // Scheduled daily at 8 AM to check for low stock and expired medicines
    @Scheduled(cron = "0 0 8 * * *")
    public void checkStockAlerts() {
        System.out.println("Running daily stock checks...");

        List<Inventory> lowStock = inventoryRepository.findByStockQuantityLessThanEqual(DEFAULT_LOW_STOCK_THRESHOLD);
        for (Inventory item : lowStock) {
            System.out.println("ALERT: Low stock for " + item.getMedicine().getName() + " (Batch: "
                    + item.getBatchNumber() + "). Quantity: " + item.getStockQuantity());
            // We could trigger an email to the pharmacist here
        }

        List<Inventory> expired = inventoryRepository.findByExpiryDateBefore(LocalDate.now());
        for (Inventory item : expired) {
            System.out.println("WARNING: Expired medicine " + item.getMedicine().getName() + " (Batch: "
                    + item.getBatchNumber() + "). Expired on: " + item.getExpiryDate());
            // We could trigger an email to the pharmacist here
        }
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

    private InventoryResponse mapToResponse(Inventory inventory) {
        InventoryResponse resp = new InventoryResponse();
        resp.setId(inventory.getId());
        resp.setMedicineName(inventory.getMedicine().getName());
        resp.setBatchNumber(inventory.getBatchNumber());
        resp.setStockQuantity(inventory.getStockQuantity());
        resp.setExpiryDate(inventory.getExpiryDate());
        return resp;
    }
}
