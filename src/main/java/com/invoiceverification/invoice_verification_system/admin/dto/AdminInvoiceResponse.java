package com.invoiceverification.invoice_verification_system.admin.dto;
import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminInvoiceResponse(Long id, String invoiceNumber, String referenceNumber, String sku,
                                   LocalDate purchaseDate, String name, String email, String phone,
                                   String country, String purchasedFrom, String verificationStatus,
                                   LocalDateTime createdAt) {
    public static AdminInvoiceResponse from(Invoice invoice) {
        return new AdminInvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getReferenceNumber(), invoice.getSku(),
                invoice.getPurchaseDate(), invoice.getName(), invoice.getEmail(), invoice.getPhone(), invoice.getCountry(),
                invoice.getPurchasedFrom(), invoice.getVerificationStatus().name(), invoice.getCreatedAt());
    }
}
