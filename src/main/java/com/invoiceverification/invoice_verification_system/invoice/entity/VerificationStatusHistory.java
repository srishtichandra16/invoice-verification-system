package com.invoiceverification.invoice_verification_system.invoice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_verification_history")
public class VerificationStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "invoice_id", nullable = false) private Invoice invoice;
    @Column(nullable = false, length = 20) private String oldStatus;
    @Column(nullable = false, length = 20) private String newStatus;
    @Column(nullable = false, length = 255) private String changedBy;
    @Column(length = 1000) private String reviewNote;
    @Column(nullable = false, updatable = false) private LocalDateTime changedAt;
    @PrePersist void created() { changedAt = LocalDateTime.now(); }
    public void setInvoice(Invoice value) { invoice = value; }
    public void setOldStatus(String value) { oldStatus = value; }
    public void setNewStatus(String value) { newStatus = value; }
    public void setChangedBy(String value) { changedBy = value; }
    public void setReviewNote(String value) { reviewNote = value; }
}
