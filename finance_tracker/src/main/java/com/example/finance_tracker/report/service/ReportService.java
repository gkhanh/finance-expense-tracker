package com.example.finance_tracker.report.service;

import com.example.finance_tracker.expense.repository.ExpenseRepository;
import com.example.finance_tracker.revenue.repository.RevenueRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.finance_tracker.expense.model.Expense;
import com.example.finance_tracker.revenue.model.Revenue;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final RevenueRepository revenueRepository;

    public ReportService(ExpenseRepository expenseRepository, RevenueRepository revenueRepository) {
        this.expenseRepository = expenseRepository;
        this.revenueRepository = revenueRepository;
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Calculates summary stats for the 3 KPI boxes.
     * Defaults to the latest month with data, not necessarily the current calendar month.
     */
    public Map<String, Object> getDashboardSummary() {
        String userId = getCurrentUserId();
        
        // Find the latest date with data to determine "Current Month"
        LocalDate latestDate = findLatestTransactionDate(userId);
        
        // --- 1. Lifetime Net Balance ---
        double totalRevenue = revenueRepository.findByUserId(userId).stream()
            .mapToDouble(Revenue::getAmount).sum();
        double totalExpense = expenseRepository.findByUserId(userId).stream()
            .mapToDouble(Expense::getAmount).sum();
        double netBalance = totalRevenue - totalExpense;

        // --- 2. Monthly Calculations (Based on Latest Data Month) ---
        LocalDate startOfCurrentMonth = YearMonth.from(latestDate).atDay(1);
        LocalDate endOfCurrentMonth = YearMonth.from(latestDate).atEndOfMonth();
        
        LocalDate startOfLastMonth = YearMonth.from(latestDate).minusMonths(1).atDay(1);
        LocalDate endOfLastMonth = YearMonth.from(latestDate).minusMonths(1).atEndOfMonth();

        // Latest Month Flow
        double currentMonthIncome = revenueRepository.findByUserIdAndDateBetween(userId, startOfCurrentMonth, endOfCurrentMonth).stream()
            .mapToDouble(Revenue::getAmount).sum();
        double currentMonthExpense = expenseRepository.findByUserIdAndDateBetween(userId, startOfCurrentMonth, endOfCurrentMonth).stream()
            .mapToDouble(Expense::getAmount).sum();
        
        // Previous Month Flow
        double lastMonthIncome = revenueRepository.findByUserIdAndDateBetween(userId, startOfLastMonth, endOfLastMonth).stream()
            .mapToDouble(Revenue::getAmount).sum();
        double lastMonthExpense = expenseRepository.findByUserIdAndDateBetween(userId, startOfLastMonth, endOfLastMonth).stream()
            .mapToDouble(Expense::getAmount).sum();

        // --- 3. Trends ---
        double incomeTrend = calculateTrend(currentMonthIncome, lastMonthIncome);
        double expenseTrend = calculateTrend(currentMonthExpense, lastMonthExpense);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("netBalance", netBalance);
        
        summary.put("monthlyIncome", currentMonthIncome);
        summary.put("incomeTrend", incomeTrend);
        
        summary.put("monthlyExpense", currentMonthExpense);
        summary.put("expenseTrend", expenseTrend);
        
        summary.put("monthName", latestDate.format(DateTimeFormatter.ofPattern("MMM"))); // Return Month Name

        return summary;
    }

    private LocalDate findLatestTransactionDate(String userId) {
        Optional<Expense> lastExpense = expenseRepository.findFirstByUserIdOrderByDateDesc(userId);
        Optional<Revenue> lastRevenue = revenueRepository.findFirstByUserIdOrderByDateDesc(userId);
        
        if (lastExpense.isPresent() && lastRevenue.isPresent()) {
            return lastExpense.get().getDate().isAfter(lastRevenue.get().getDate()) 
                ? lastExpense.get().getDate() 
                : lastRevenue.get().getDate();
        } else if (lastExpense.isPresent()) {
            return lastExpense.get().getDate();
        } else if (lastRevenue.isPresent()) {
            return lastRevenue.get().getDate();
        }
        return LocalDate.now(); // Default to now if no data
    }

    private double calculateTrend(double current, double previous) {
        if (previous == 0) {
            return current == 0 ? 0.0 : 100.0;
        }
        return ((current - previous) / Math.abs(previous)) * 100;
    }

    /**
     * Returns monthly GROWTH PERCENTAGE for the last 6 months ending at LATEST DATA MONTH.
     */
    public List<Map<String, Object>> getSixMonthTrend() {
        String userId = getCurrentUserId();
        LocalDate latestDate = findLatestTransactionDate(userId); // Use same logic
        
        List<Map<String, Object>> trendData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

        // We need 7 months of data to calculate 6 months of trends
        List<Double> incomeValues = new ArrayList<>();
        List<Double> expenseValues = new ArrayList<>();
        List<String> monthNames = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(latestDate).minusMonths(i);
            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.atEndOfMonth();

            double monthlyIncome = revenueRepository.findByUserIdAndDateBetween(userId, start, end).stream()
                .mapToDouble(Revenue::getAmount).sum();
                
            double monthlyExpense = expenseRepository.findByUserIdAndDateBetween(userId, start, end).stream()
                .mapToDouble(Expense::getAmount).sum();
            
            incomeValues.add(monthlyIncome);
            expenseValues.add(monthlyExpense);
            monthNames.add(yearMonth.format(formatter));
        }

        // Calculate trends for indices 1 to 6 (last 6 months)
        for (int i = 1; i < 7; i++) {
            double prevIncome = incomeValues.get(i-1);
            double currIncome = incomeValues.get(i);
            double incomeGrowth = calculateTrend(currIncome, prevIncome);

            double prevExpense = expenseValues.get(i-1);
            double currExpense = expenseValues.get(i);
            double expenseGrowth = calculateTrend(currExpense, prevExpense);

            Map<String, Object> point = new HashMap<>();
            point.put("month", monthNames.get(i));
            point.put("income", incomeGrowth);
            point.put("expense", expenseGrowth);
            trendData.add(point);
        }
        
        return trendData;
    }

    public Map<String, Double> getCategoryBreakdown() {
        String userId = getCurrentUserId();
        List<Expense> allUserExpenses = expenseRepository.findByUserId(userId); 
        return allUserExpenses.stream()
            .collect(Collectors.groupingBy(
                Expense::getCategory, 
                Collectors.summingDouble(Expense::getAmount)
            ));
    }
}
