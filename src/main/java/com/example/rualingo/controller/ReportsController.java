package com.example.rualingo.controller;

import com.example.rualingo.service.ReportsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateReport() {
        Map<String, Object> report = reportsService.generateSystemReport();
        return ResponseEntity.ok(report);
    }
}