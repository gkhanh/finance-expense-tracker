package com.example.finance_tracker.auth.repository;

import com.example.finance_tracker.auth.model.TwoFactorSetupToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TwoFactorSetupRepository extends MongoRepository<TwoFactorSetupToken, String> {
    Optional<TwoFactorSetupToken> findByToken(String token);
    Optional<TwoFactorSetupToken> findByUsername(String username);
    void deleteByUsername(String username);
}
