package com.invoiceverification.invoice_verification_system.invoice.repository;

import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.invoiceverification.invoice_verification_system.invoice.entity.VerificationStatus;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByIdAndBrandId(Long id, Long brandId);
    Page<Invoice> findAllByBrandId(Long brandId, Pageable pageable);
    long countByBrandId(Long brandId);
    long countByBrandIdAndVerificationStatus(Long brandId, VerificationStatus status);
}
