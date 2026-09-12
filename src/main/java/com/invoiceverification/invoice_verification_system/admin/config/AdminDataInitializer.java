package com.invoiceverification.invoice_verification_system.admin.config;

import com.invoiceverification.invoice_verification_system.admin.entity.AdminUser;
import com.invoiceverification.invoice_verification_system.admin.repository.AdminUserRepository;
import com.invoiceverification.invoice_verification_system.brand.service.BrandService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataInitializer {
    @Bean
    CommandLineRunner seedAdminUsers(@Value("${app.seed-admins:false}") boolean enabled,
                                     @Value("${app.seed-admin.brand-slug:aigner}") String brandSlug,
                                     @Value("${app.seed-admin.email:}") String email,
                                     @Value("${app.seed-admin.password:}") String password,
                                     BrandService brands, AdminUserRepository admins, PasswordEncoder encoder) {
        return ignored -> {
            if (!enabled) return;
            if (email.isBlank() || password.length() < 14) {
                throw new IllegalStateException("Admin seed email is required and password must contain at least 14 characters");
            }
            create(brandSlug, email, password, brands, admins, encoder);
        };
    }
    private void create(String slug, String email, String password, BrandService brands, AdminUserRepository admins, PasswordEncoder encoder) {
        if (admins.findByEmailIgnoreCase(email).isPresent()) return;
        AdminUser user = new AdminUser(); user.setBrand(brands.getActiveBrandBySlug(slug)); user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(encoder.encode(password)); admins.save(user);
    }
}
