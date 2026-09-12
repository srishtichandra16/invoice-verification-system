package com.invoiceverification.invoice_verification_system.brand.controller;

import com.invoiceverification.invoice_verification_system.brand.dto.BrandResponse;
import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.brand.service.BrandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping("/{brandSlug}")
    public BrandResponse getBrand(
            @PathVariable String brandSlug
    ) {
        Brand brand =
                brandService.getActiveBrandBySlug(brandSlug);

        return new BrandResponse(
                brand.getSlug(),
                brand.getDisplayName()
        );
    }
}
