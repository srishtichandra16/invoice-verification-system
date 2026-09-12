package com.invoiceverification.invoice_verification_system.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BrandPageController {
    @GetMapping("/{brandSlug}/register") public String register() { return "forward:/index.html"; }
    @GetMapping("/{brandSlug}/admin") public String admin() { return "forward:/admin.html"; }
}
