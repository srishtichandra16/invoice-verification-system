package com.invoiceverification.invoice_verification_system.brand.service;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.brand.repository.BrandRepository;
import org.springframework.stereotype.Service;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public Brand getActiveBrandBySlug(String slug) {

        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException(
                    "Brand is required"
            );
        }

        return brandRepository
                .findBySlugIgnoreCaseAndActiveTrue(slug.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Brand not found or inactive"
                        )
                );
    }
}
