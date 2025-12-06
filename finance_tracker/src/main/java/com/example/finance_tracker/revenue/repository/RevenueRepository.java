package com.example.finance_tracker.revenue.repository;

import com.example.finance_tracker.revenue.model.Revenue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RevenueRepository extends MongoRepository<Revenue, String> {

    List<Revenue> findByUserId(String userId);

    //Find revenues for a user between two dates (Inclusive)
    @Query("{ 'userId' : ?0, 'date' : { $gte: ?1, $lte: ?2 } }")
    List<Revenue> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    // Find latest revenue
    Optional<Revenue> findFirstByUserIdOrderByDateDesc(String userId);
}
