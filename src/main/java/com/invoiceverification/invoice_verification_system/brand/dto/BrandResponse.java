package com.invoiceverification.invoice_verification_system.brand.dto;

public class BrandResponse {

    private final String slug;
    private final String displayName;

    public BrandResponse(
            String slug,
            String displayName
    ) {
        this.slug = slug;
        this.displayName = displayName;
    }

    public String getSlug() {
        return slug;
    }

    public String getDisplayName() {
        return displayName;
    }
}
