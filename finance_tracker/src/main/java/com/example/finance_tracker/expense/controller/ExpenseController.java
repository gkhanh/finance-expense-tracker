package com.example.finance_tracker.expense.controller;

import com.example.finance_tracker.expense.model.Expense;
import com.example.finance_tracker.expense.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@PreAuthorize("hasRole('USER')") // Apply base security to all methods in this controller
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
        @RequestParam(required = false) LocalDate startDate, 
        @RequestParam(required = false) LocalDate endDate
    ) {
        List<Expense> expenses;
        
        if (startDate != null && endDate != null) {
            expenses = expenseService.getExpensesBetweenDates(startDate, endDate); 
        } else {
            expenses = expenseService.getExpenses();
        }
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpense(@PathVariable String id) {
        return ResponseEntity.ok(expenseService.getExpense(id));
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody Expense expense) {
        Expense savedExpense = expenseService.saveExpense(expense);
        return ResponseEntity.ok(savedExpense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable String id, @Valid @RequestBody Expense expense) {
        Expense updatedExpense = expenseService.updateExpense(id, expense);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build(); 
    }
}
