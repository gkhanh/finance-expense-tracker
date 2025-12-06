package com.example.finance_tracker.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.finance_tracker.auth.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
