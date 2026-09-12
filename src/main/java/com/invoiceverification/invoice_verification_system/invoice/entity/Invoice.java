package com.invoiceverification.invoice_verification_system.invoice.entity;

import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(length = 100) private String invoiceNumber;
    @Column(length = 100) private String referenceNumber;
    @Column(length = 100) private String sku;
    private LocalDate purchaseDate;
    @Column(columnDefinition = "text") private String customData;
    @Column(length = 150) private String name;
    @Column(length = 255) private String email;
    @Column(length = 50) private String phone;
    @Column(length = 100) private String country;
    @Column(length = 255) private String purchasedFrom;
    private Boolean marketingConsent;
    @Column(nullable = false, length = 500) private String objectKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Version @Column(nullable = false) private long version;

    @PrePersist void created() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String value) { invoiceNumber = value; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String value) { referenceNumber = value; }
    public String getSku() { return sku; }
    public void setSku(String value) { sku = value; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate value) { purchaseDate = value; }
    public String getCustomData() { return customData; }
    public void setCustomData(String value) { customData = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { country = value; }
    public String getPurchasedFrom() { return purchasedFrom; }
    public void setPurchasedFrom(String value) { purchasedFrom = value; }
    public Boolean getMarketingConsent() { return marketingConsent; }
    public void setMarketingConsent(Boolean value) { marketingConsent = value; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String value) { objectKey = value; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus value) { verificationStatus = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
