package com.invoiceverification.invoice_verification_system.health;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/system/health")
public class HealthController {
    private final JdbcTemplate jdbc;
    public HealthController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @GetMapping("/live") public Map<String, String> live() { return Map.of("status", "UP"); }
    @GetMapping("/ready") public ResponseEntity<Map<String, String>> ready() {
        try { jdbc.queryForObject("select 1", Integer.class); return ResponseEntity.ok(Map.of("status", "UP")); }
        catch (RuntimeException exception) { return ResponseEntity.status(503).body(Map.of("status", "DOWN")); }
    }
}
