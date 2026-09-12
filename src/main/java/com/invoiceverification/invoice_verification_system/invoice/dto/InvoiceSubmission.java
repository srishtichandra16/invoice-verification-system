package com.invoiceverification.invoice_verification_system.invoice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class InvoiceSubmission {

    @Size(max = 100) private String invoiceNumber;
    @Size(max = 100) private String referenceNumber;
    @Size(max = 100) private String sku;

    @NotNull(message = "Purchase date is required") @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    @Size(max = 5000) private String customData;

    // Customer information
    @Size(max = 150) private String name;
    @Email(message = "Enter a valid email address") @Size(max = 255) private String email;
    @Size(max = 50) private String phone;
    @Size(max = 100) private String country;

    // Product information
    @Size(max = 255) private String purchasedFrom;

    // Marketing preference
    private Boolean marketingConsent;

    // Invoice number
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }


    // Reference number
    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }


    // SKU
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    // Purchase date
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }


    // Custom data
    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }


    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // Phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    // Country
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    // Purchased from
    public String getPurchasedFrom() {
        return purchasedFrom;
    }

    public void setPurchasedFrom(String purchasedFrom) {
        this.purchasedFrom = purchasedFrom;
    }


    // Marketing consent
    public Boolean getMarketingConsent() {
        return marketingConsent;
    }

    public void setMarketingConsent(Boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }
}
