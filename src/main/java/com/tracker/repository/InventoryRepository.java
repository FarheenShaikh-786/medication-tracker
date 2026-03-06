package com.tracker.repository;

import com.tracker.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByPharmacistProfileId(Long pharmacistProfileId);

    Optional<Inventory> findByPharmacistProfileIdAndMedicineIdAndBatchNumber(Long pharmacistProfileId, Long medicineId,
            String batchNumber);

    List<Inventory> findByStockQuantityLessThanEqual(int quantity);

    List<Inventory> findByExpiryDateBefore(LocalDate date);
}
