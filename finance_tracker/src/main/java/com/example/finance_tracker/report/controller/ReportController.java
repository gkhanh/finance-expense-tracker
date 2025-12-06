package com.example.finance_tracker.report.controller;

import com.example.finance_tracker.report.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('USER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(reportService.getDashboardSummary());
    }
    
    // New endpoint for chart data
    @GetMapping("/trend")
    public ResponseEntity<List<Map<String, Object>>> getTrend() {
        return ResponseEntity.ok(reportService.getSixMonthTrend());
    }

    @GetMapping("/breakdown")
    public ResponseEntity<Map<String, Double>> getCategoryBreakdown() {
        return ResponseEntity.ok(reportService.getCategoryBreakdown());
    }
}
