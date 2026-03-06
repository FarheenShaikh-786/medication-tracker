package com.tracker.repository;

import com.tracker.entity.PharmacistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacistProfileRepository extends JpaRepository<PharmacistProfile, Long> {
    Optional<PharmacistProfile> findByUserId(Long userId);
}
