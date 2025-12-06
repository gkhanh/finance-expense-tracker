package com.example.finance_tracker.revenue.service;

import com.example.finance_tracker.revenue.model.Revenue;
import com.example.finance_tracker.revenue.repository.RevenueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

import java.util.List;

@Service
public class RevenueService {

    private final RevenueRepository revenueRepository;

    public RevenueService(RevenueRepository revenueRepository) {
        this.revenueRepository = revenueRepository;
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName(); 
    }

    public Revenue saveRevenue(Revenue revenue) {
        String userId = getCurrentUserId();
        revenue.setUserId(userId); 
        return revenueRepository.save(revenue);
    }

    public List<Revenue> getRevenues() {
        String userId = getCurrentUserId();
        return revenueRepository.findByUserId(userId);
    }

    public Revenue getRevenue(String id) {
        Revenue revenue = revenueRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenue not found"));
            
        if (!revenue.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Revenue does not belong to user.");
        }
        return revenue;
    }

    //Filter revenues by date
    public List<Revenue> getRevenuesBetweenDates(LocalDate startDate, LocalDate endDate) {
        String userId = getCurrentUserId();
        return revenueRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    //Update an existing revenue
    public Revenue updateRevenue(String id, Revenue updatedRevenue) {
        Revenue existingRevenue = revenueRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenue not found"));

        //Ensure the revenue belongs to the current user
        if (!existingRevenue.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Revenue does not belong to user.");
        }

        existingRevenue.setAmount(updatedRevenue.getAmount());
        existingRevenue.setSource(updatedRevenue.getSource());
        existingRevenue.setDate(updatedRevenue.getDate());
        
        return revenueRepository.save(existingRevenue);
    }

    //Delete a revenue
    public void deleteRevenue(String id) {
        Revenue existingRevenue = revenueRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revenue not found"));

        //Ensure the revenue belongs to the current user
        if (!existingRevenue.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Revenue does not belong to user.");
        }

        revenueRepository.deleteById(id);
    }
}
