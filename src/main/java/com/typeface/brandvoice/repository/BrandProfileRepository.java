package com.typeface.brandvoice.repository;

import com.typeface.brandvoice.model.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandProfileRepository extends JpaRepository<BrandProfile, String> {

    Optional<BrandProfile> findByCustomerId(String customerId);

    boolean existsByCustomerId(String customerId);
}