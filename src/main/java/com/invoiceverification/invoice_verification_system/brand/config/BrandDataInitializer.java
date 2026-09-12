package com.invoiceverification.invoice_verification_system.brand.config;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.brand.repository.BrandRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class BrandDataInitializer implements CommandLineRunner {

    private final BrandRepository brandRepository;
    private final String bootstrapBrands;

    public BrandDataInitializer(BrandRepository brandRepository,
                                @Value("${app.bootstrap-brands:aigner}") String bootstrapBrands) {
        this.brandRepository = brandRepository;
        this.bootstrapBrands = bootstrapBrands;
    }

    @Override
    public void run(String... args) {
        Set<String> enabledSlugs = new LinkedHashSet<>();
        for (String value : bootstrapBrands.split(",")) {
            String slug = value.trim().toLowerCase(Locale.ROOT);
            if (slug.isEmpty()) continue;
            if (!slug.matches("[a-z0-9][a-z0-9-]{1,49}")) {
                throw new IllegalStateException("Invalid brand slug in APP_BOOTSTRAP_BRANDS: " + slug);
            }
            enabledSlugs.add(slug);
            createBrandIfMissing(slug, slug.replace('-', ' ').toUpperCase(Locale.ROOT));
        }
        for (Brand brand : brandRepository.findAll()) {
            boolean shouldBeActive = enabledSlugs.contains(brand.getSlug().toLowerCase(Locale.ROOT));
            if (Boolean.TRUE.equals(brand.getActive()) != shouldBeActive) {
                brand.setActive(shouldBeActive);
                brandRepository.save(brand);
            }
        }
    }

    private void createBrandIfMissing(
            String slug,
            String displayName
    ) {
        boolean brandExists =
                brandRepository.existsBySlugIgnoreCase(slug);

        if (!brandExists) {
            Brand brand = new Brand(slug, displayName);
            brandRepository.save(brand);
        }
    }
}
