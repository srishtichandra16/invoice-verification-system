package com.invoiceverification.invoice_verification_system.brand.repository;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findBySlugIgnoreCaseAndActiveTrue(String slug);
    boolean existsBySlugIgnoreCase(String slug);
}
