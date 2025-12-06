package com.example.finance_tracker.expense.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;
    private String userId; 

    @NotNull(message = "Amount is required.") 
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
    private double amount;

    @NotBlank(message = "Category is required.")
    @Size(max = 50, message = "Category cannot exceed 50 characters.")
    private String category; // e.g., "Food", "Rent"
    
    @NotBlank(message = "Description is required.")
    private String description;

    @NotNull(message = "Date is required.")
    private LocalDate date;

    // Default Constructor
    public Expense() {
        this.date = LocalDate.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}