package com.invoiceverification.invoice_verification_system.invoice.repository;
import com.invoiceverification.invoice_verification_system.invoice.entity.VerificationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VerificationStatusHistoryRepository extends JpaRepository<VerificationStatusHistory, Long> { }
