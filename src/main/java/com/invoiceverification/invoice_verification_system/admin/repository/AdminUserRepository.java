package com.invoiceverification.invoice_verification_system.admin.repository;

import com.invoiceverification.invoice_verification_system.admin.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByEmailIgnoreCase(String email);
}
