package com.invoiceverification.invoice_verification_system.invoice.repository;

import com.invoiceverification.invoice_verification_system.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

}

