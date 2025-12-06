package com.example.finance_tracker.revenue.controller;

import com.example.finance_tracker.revenue.model.Revenue;
import com.example.finance_tracker.revenue.service.RevenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/revenues")
@PreAuthorize("hasRole('USER')") // Apply base security to all methods in this controller
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping
    public ResponseEntity<List<Revenue>> getRevenues(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ) {
        List<Revenue> revenues;
        
        if (startDate != null && endDate != null) {
            revenues = revenueService.getRevenuesBetweenDates(startDate, endDate); 
        } else {
            revenues = revenueService.getRevenues();
        }
        return ResponseEntity.ok(revenues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Revenue> getRevenue(@PathVariable String id) {
        return ResponseEntity.ok(revenueService.getRevenue(id));
    }

    @PostMapping
    public ResponseEntity<Revenue> createRevenue(@Valid @RequestBody Revenue revenue) {
        Revenue savedRevenue = revenueService.saveRevenue(revenue);
        return ResponseEntity.ok(savedRevenue);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Revenue> updateRevenue(@PathVariable String id, @Valid @RequestBody Revenue revenue) {
        Revenue updatedRevenue = revenueService.updateRevenue(id, revenue);
        return ResponseEntity.ok(updatedRevenue);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRevenue(@PathVariable String id) {
        revenueService.deleteRevenue(id);
        return ResponseEntity.noContent().build(); 
    }
}
