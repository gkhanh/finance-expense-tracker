package com.example.finance_tracker.expense.service;

import com.example.finance_tracker.expense.model.Expense;
import com.example.finance_tracker.expense.repository.ExpenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName(); 
    }

    public Expense saveExpense(Expense expense) {
        String userId = getCurrentUserId();
        expense.setUserId(userId); 
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpenses() {
        String userId = getCurrentUserId();
        return expenseRepository.findByUserId(userId);
    }

    public Expense getExpense(String id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
            
        if (!expense.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Expense does not belong to user.");
        }
        return expense;
    }

    //Filter expenses by date
    public List<Expense> getExpensesBetweenDates(LocalDate startDate, LocalDate endDate) {
        String userId = getCurrentUserId();
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public Expense updateExpense(String id, Expense updatedExpense) {
        Expense existingExpense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        if (!existingExpense.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Expense does not belong to user.");
        }

        existingExpense.setAmount(updatedExpense.getAmount());
        existingExpense.setCategory(updatedExpense.getCategory());
        existingExpense.setDescription(updatedExpense.getDescription());
        existingExpense.setDate(updatedExpense.getDate());
        
        return expenseRepository.save(existingExpense);
    }

    public void deleteExpense(String id) {
        Expense existingExpense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        if (!existingExpense.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Expense does not belong to user.");
        }

        expenseRepository.deleteById(id);
    }
}
