# My Finance Tracker API

A secure, modular, RESTful API for personal finance tracking, built with Spring Boot and MongoDB.

## 🛠️ Architecture Overview

This project uses a standard **Layered and Modular Architecture** pattern. Its core principles are:

1.  **Security First:** All data access is secured by JWT authentication and strict **User Data Isolation**.
2.  **Modularity:** Code is separated into functional packages (auth, expense, revenue, report) for maintainability.

## Core Features Implemented

The API is fully functional and includes the following features:

| Feature | Endpoint(s) | Description |
| :--- | :--- | :--- |
| **User Authentication** | `POST /api/auth/register`, `POST /api/auth/login` | Secure user registration and JWT token generation. |
| **Data Isolation** | All secured endpoints | Ensures a user can **only** access, modify, or delete their own transactions. |
| **Full CRUD** | `POST`, `GET`, `PUT`, `DELETE` on `/api/expenses`, `/api/revenues` | Complete management of user transactions. |
| **Data Filtering** | `GET /api/expenses?startDate=...` | Allows querying transactions within a specified date range. |
| **Reporting: Net Balance** | `GET /api/reports/balance` | Calculates and returns `Total Revenue - Total Expense`. |
| **Reporting: Breakdown** | `GET /api/reports/breakdown` | Calculates total spending grouped by category. |
| **Input Validation** | `POST/PUT` on all transaction endpoints | Ensures data integrity (e.g., amounts are positive, required fields are not empty). |

## Project Structure Breakdown

| Folder | Package | Layer | Key Responsibility |
| :--- | :--- | :--- | :--- |
| `auth` | `com.example.finance_tracker.auth` | Service/Repo | Handles `User` model, registration, login, and JWT logic. |
| `expense` | `com.example.finance_tracker.expense` | All Layers | Manages the Expense Model, CRUD operations, and business rules. |
| `revenue` | `com.example.finance_tracker.revenue` | All Layers | Manages the Revenue Model, CRUD operations, and business rules. |
| **`report`** | `com.example.finance_tracker.report` | Service/Controller | **Business Intelligence:** Calculates aggregates (e.g., Net Balance, Category Totals). |
| **`config`** | `com.example.finance_tracker.config` | Cross-Cutting | Centralized configuration for Security (`SecurityConfig`) and Error Handling (`GlobalExceptionHandler`). |

## Dependencies

* **Spring Boot Web** (REST API)
* **Spring Data MongoDB** (Database access)
* **Spring Security** (Authentication, Authorization, JWT)
* **JWT Library** (Token creation and validation)
* **Validation** (`jakarta.validation`)

## Future Development Notes

1.  **User Roles:** Expand the `User` model to include roles (e.g., ADMIN, GUEST).
2.  **Testing:** Implement Unit and Integration tests for all Service and Controller methods.
3.  **UI Development:** Connect a separate frontend application (e.g., React, Vue, Angular) to consume these endpoints.


1. The Core Transaction Flow (Saving an Expense)
This flow shows how the Security (Auth) layer works with the Data Isolation principle.
+--------------------------------+
|      CLIENT (Frontend/Curl)    |
+--------------+-----------------+
               | HTTP POST /api/expenses (Expense Data + JWT Token)
               v
+--------------------------------+
|  1. CONFIG: Security Filter    |
|   (AuthTokenFilter.java)       |
|--------------------------------|
| - Action: Validates JWT token.
| - Success: Puts 'userId' into SecurityContextHolder.
+--------------+-----------------+
               | Request Authorized (Context Set)
               v
+--------------------------------+
|  2. EXPENSE: Controller Layer  |
|   (ExpenseController.java)     |
|--------------------------------|
| - Action: Executes @Valid (Input Validation).
| - Calls: expenseService.saveExpense(expense)
+--------------+-----------------+
               |
               v
+--------------------------------+
|  3. EXPENSE: Service Layer     |
|   (ExpenseService.java)        |
|--------------------------------|
| - Action 1: Gets User ID: getCurrentUserId()
| - Action 2: Enforces Ownership: expense.setUserId(userId)
| - Calls: expenseRepository.save(expense)
+--------------+-----------------+
               |
               v
+--------------------------------+
|  4. EXPENSE: Repository Layer  |
|   (ExpenseRepository.java)     |
|--------------------------------|
| - Action: MongoDB Driver executes INSERT query.
+--------------+-----------------+
               |
               v
+--------------------------------+
|  5. DATABASE (MongoDB)         |
+--------------------------------+

2. The Reporting Flow (Calculating Net Balance)
This flow demonstrates the power of the Report Module to aggregate data from multiple transactional modules.
+--------------------------------+
|      CLIENT (Frontend/Curl)    |
+--------------+-----------------+
               | HTTP GET /api/reports/balance (Token Required)
               v
+--------------------------------+
|  1. REPORT: Controller Layer   |
|   (ReportController.java)      |
|--------------------------------|
| - Calls: reportService.calculateNetBalance()
+--------------+-----------------+
               |
               v
+--------------------------------+
|  2. REPORT: Service Layer      |
|   (ReportService.java)         |
|--------------------------------|
| - Action 1: Gets userId from Context.
| - Action 2: Calls RevenueRepository.findByUserId(userId)
| - Action 3: Calls ExpenseRepository.findByUserId(userId)
| - Action 4: Calculates: Total Revenue - Total Expense
+----------+----------+-----------------+
           |          | Returns calculated balance to Controller
           v          v
+----------+----------+-----------------+
| 3a. REVENUE: Repository | 3b. EXPENSE: Repository |
| (RevenueRepository.java) | (ExpenseRepository.java) |
+----------+----------+-----------------+
           |          |
           v          v
+----------+----------+-----------------+
|       4. DATABASE (MongoDB)         |
+-------------------------------------+


SecurityConfig.java(config) - Global Rules Defines which URLs are public (/api/auth) and which require authentication, and correctly injects the AuthTokenFilter.
GlobalExceptionHandler.java(config) - Error Robustness: Catches errors (like validation failure) application-wide and returns clean 400 Bad Request JSON responses.
JwtUtils.java(auth) - Token Utility: Creates, validates, and extracts usernames from JWT tokens, supporting the security layer.
User.java(auth/model) - Identity: The source of truth for user credentials and the userId used for all isolation queries.