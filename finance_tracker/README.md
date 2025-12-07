# Finance Tracker API

A secure, modular, RESTful API for personal finance tracking, built with the latest Spring Boot ecosystem and MongoDB. This application provides a robust backend for managing personal finances, featuring advanced security, detailed reporting, and data isolation.

## 🚀 Tech Stack

This project leverages a modern and powerful technology stack:

*   **Language:** Java 25
*   **Framework:** Spring Boot 4.0.0 (Snapshot)
    *   **Web:** Spring Web (REST API)
    *   **Data:** Spring Data MongoDB
    *   **Security:** Spring Security & OAuth2 Client
    *   **Validation:** Jakarta Validation
    *   **Mail:** Spring Boot Starter Mail (Password Resets)
*   **Database:** MongoDB
*   **Authentication:**
    *   JWT (JSON Web Tokens) for stateless session management
    *   Google OAuth2 for social login
    *   Google Authenticator (TOTP) for Two-Factor Authentication (2FA)
*   **Build Tool:** Gradle
*   **Containerization:** Docker & Docker Compose
*   **Utilities:** Lombok

## 🛠️ Core Features & API Functions

The API is organized into functional modules. Below is a detailed list of all available functions and endpoints.

### 1. Authentication & Security Module (`/api/auth`)
Handles user identity, access control, and security features.

*   **User Registration:** `POST /api/auth/register` - Creates a new user account with secure password hashing.
*   **Login (Local):** `POST /api/auth/login` - Authenticates username/password. Initiates 2FA setup if not enabled, or requests 2FA code if enabled.
*   **Verify 2FA (Local):** `POST /api/auth/verify-2fa` - Verifies TOTP code for local login and issues JWT.
*   **OAuth2 Login (Google):** `POST /api/auth/oauth` - Authenticates via Google ID token. Handles 2FA flow for social users.
*   **Verify 2FA (OAuth):** `POST /api/auth/verify-2fa-oauth` - Verifies TOTP code for OAuth login and issues JWT.
*   **Forgot Password:** `POST /api/auth/forgot-password` - Sends a password reset verification code via email.
*   **Reset Password:** `POST /api/auth/reset-password` - Resets the user's password using the verification code.
*   **Get Config:** `GET /api/auth/config` - Public endpoint to retrieve public configurations (e.g., Google Client ID).

### 2. User Management Module (`/api/users`)
Manages user profiles and account settings.

*   **Get Profile:** `GET /api/users/me` - Retrieves current user details.
*   **Update Avatar:** `POST /api/users/avatar` - Uploads and updates the user's profile picture.
*   **Remove Avatar:** `DELETE /api/users/avatar` - Deletes the user's profile picture.
*   **Delete Account:** `DELETE /api/users/me` - Permanently deletes the user's account and all associated data.

### 3. Expense Management Module (`/api/expenses`)
Full CRUD capabilities for managing expense records.

*   **List Expenses:** `GET /api/expenses` - Retrieves all expenses. Supports filtering by date range (`?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`).
*   **Get Expense:** `GET /api/expenses/{id}` - Retrieves a specific expense details.
*   **Create Expense:** `POST /api/expenses` - Adds a new expense record.
*   **Update Expense:** `PUT /api/expenses/{id}` - Updates an existing expense.
*   **Delete Expense:** `DELETE /api/expenses/{id}` - Removes an expense record.

### 4. Revenue Management Module (`/api/revenues`)
Full CRUD capabilities for managing income sources.

*   **List Revenues:** `GET /api/revenues` - Retrieves all revenue records. Supports filtering by date range.
*   **Get Revenue:** `GET /api/revenues/{id}` - Retrieves specific revenue details.
*   **Create Revenue:** `POST /api/revenues` - Adds a new revenue source.
*   **Update Revenue:** `PUT /api/revenues/{id}` - Updates an existing revenue record.
*   **Delete Revenue:** `DELETE /api/revenues/{id}` - Removes a revenue record.

### 5. Reporting & Analytics Module (`/api/reports`)
Provides aggregated insights into financial health.

*   **Dashboard Summary:** `GET /api/reports/summary` - Returns high-level metrics: Total Revenue, Total Expense, and Net Balance.
*   **Trend Analysis:** `GET /api/reports/trend` - Returns monthly financial data for the last 6 months for charting.
*   **Category Breakdown:** `GET /api/reports/breakdown` - Returns total spending grouped by expense category.

## 🏛️ Architecture Overview

The project follows a **Layered and Modular Architecture** to ensure scalability and maintainability.

*   **Security First:** Strict **User Data Isolation** ensures users can only access their own data. All protected endpoints require a valid JWT.
*   **Modular Design:** Features are encapsulated in packages (`auth`, `expense`, `revenue`, `report`), making the codebase easy to navigate and extend.
*   **Global Handling:**
    *   `GlobalExceptionHandler`: Centralized error handling for consistent API responses.
    *   `SecurityConfig`: Declarative security rules and filter chains.

### 1. The Core Transaction Flow (Saving an Expense)
This flow shows how the Security (Auth) layer works with the Data Isolation principle.
```
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
```

### 2. The Reporting Flow (Calculating Net Balance)
This flow demonstrates the power of the Report Module to aggregate data from multiple transactional modules.
```
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
```

## 📝 Future Development

1.  **Role-Based Access Control (RBAC):** Expand `User` model to fully utilize roles (ADMIN, GUEST).
2.  **Advanced Reporting:** Add year-over-year comparison and export features (PDF/CSV).
3.  **Budgeting:** Add a module to set and track monthly budgets per category.
