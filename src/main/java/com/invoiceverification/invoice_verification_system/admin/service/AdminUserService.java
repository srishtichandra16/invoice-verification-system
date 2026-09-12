package com.invoiceverification.invoice_verification_system.admin.service;

import com.invoiceverification.invoice_verification_system.admin.entity.AdminUser;
import com.invoiceverification.invoice_verification_system.admin.repository.AdminUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService implements UserDetailsService {
    private final AdminUserRepository repository;
    public AdminUserService(AdminUserRepository repository) { this.repository = repository; }
    @Override public UserDetails loadUserByUsername(String email) {
        AdminUser user = requireAdmin(email);
        return new User(user.getEmail(), user.getPasswordHash(), user.isActive(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
    public AdminUser requireAdmin(String email) {
        return repository.findByEmailIgnoreCase(email)
                .filter(AdminUser::isActive)
                .filter(user -> Boolean.TRUE.equals(user.getBrand().getActive()))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid administrator credentials"));
    }
}
