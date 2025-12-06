package com.example.finance_tracker.expense.repository;

import com.example.finance_tracker.expense.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExpenseRepository extends MongoRepository<Expense, String> {

    List<Expense> findByUserId(String userId);

    //Find expenses for a user between two dates (Inclusive)
    @Query("{ 'userId' : ?0, 'date' : { $gte: ?1, $lte: ?2 } }")
    List<Expense> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    //Aggregation method for reporting
    @Query(value = "{ 'userId' : ?0 }", fields = "{ 'category' : 1, 'amount' : 1, '_id' : 0 }")
    List<Map<String, Object>> findTotalExpenseByCategory(String userId);

    // Find latest expense
    Optional<Expense> findFirstByUserIdOrderByDateDesc(String userId);
}
