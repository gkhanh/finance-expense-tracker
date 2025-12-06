package com.example.finance_tracker;

import com.example.finance_tracker.auth.model.User;
import com.example.finance_tracker.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class FinanceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceTrackerApplication.class, args);
	}

	@Bean
	public CommandLineRunner cleanupDuplicateUsers(UserRepository userRepository, MongoTemplate mongoTemplate) {
		return args -> {
			// Find all users
			List<User> users = userRepository.findAll();
			
			// Group by email
			Map<String, List<User>> usersByEmail = users.stream()
				.collect(Collectors.groupingBy(User::getEmail));
			
			// Iterate and remove duplicates
			usersByEmail.forEach((email, userList) -> {
				if (userList.size() > 1) {
					System.out.println("Found duplicate users for email: " + email);
					// Keep the first one (or the one with ID if you prefer logic, but first is fine for cleanup)
					// Sort by ID to be deterministic if needed, or just keep index 0
					for (int i = 1; i < userList.size(); i++) {
						User duplicate = userList.get(i);
						System.out.println("Deleting duplicate user ID: " + duplicate.getId());
						userRepository.deleteById(duplicate.getId());
					}
				}
			});
		};
	}
}
