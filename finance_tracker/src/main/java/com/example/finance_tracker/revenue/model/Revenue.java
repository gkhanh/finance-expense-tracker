package com.example.finance_tracker.revenue.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Document(collection = "revenues")
public class Revenue {

    @Id
    private String id;
    
    // CRITICAL: Links the revenue to the user who created it
    private String userId; 

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
    private double amount;

    @NotBlank(message = "Source is required.")
    @Size(max = 50, message = "Source cannot exceed 50 characters.")
    private String source; // e.g., "Salary", "Bonus"

    @NotNull(message = "Date is required.")
    private LocalDate date;

    // Default Constructor
    public Revenue() {
        this.date = LocalDate.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
