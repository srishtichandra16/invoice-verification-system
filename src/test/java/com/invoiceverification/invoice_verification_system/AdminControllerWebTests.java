package com.invoiceverification.invoice_verification_system;

import com.invoiceverification.invoice_verification_system.admin.controller.AdminController;
import com.invoiceverification.invoice_verification_system.admin.entity.AdminUser;
import com.invoiceverification.invoice_verification_system.admin.service.AdminUserService;
import com.invoiceverification.invoice_verification_system.brand.entity.Brand;
import com.invoiceverification.invoice_verification_system.config.SecurityConfig;
import com.invoiceverification.invoice_verification_system.invoice.repository.InvoiceRepository;
import com.invoiceverification.invoice_verification_system.invoice.repository.VerificationStatusHistoryRepository;
import com.invoiceverification.invoice_verification_system.invoice.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerWebTests {
    @Autowired MockMvc mvc;
    @MockitoBean AdminUserService admins;
    @MockitoBean InvoiceRepository invoices;
    @MockitoBean S3Service s3;
    @MockitoBean VerificationStatusHistoryRepository history;
    private Brand aigner;

    @BeforeEach void setUp() {
        aigner = new Brand("aigner", "AIGNER"); ReflectionTestUtils.setField(aigner, "id", 7L);
        AdminUser admin = new AdminUser(); admin.setBrand(aigner); admin.setEmail("admin@aigner.test");
        when(admins.requireAdmin("admin@aigner.test")).thenReturn(admin);
    }

    @Test void anonymousAdminRequestIsRejected() throws Exception {
        mvc.perform(get("/api/admin/me")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(username = "admin@aigner.test", roles = "ADMIN")
    void invoiceListIsAlwaysScopedToAuthenticatedAdminsBrand() throws Exception {
        when(invoices.findAllByBrandId(org.mockito.ArgumentMatchers.eq(7L), any())).thenReturn(new PageImpl<>(List.of()));
        mvc.perform(get("/api/admin/invoices")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        verify(invoices).findAllByBrandId(org.mockito.ArgumentMatchers.eq(7L), any());
    }
}
