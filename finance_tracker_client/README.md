# Finance Tracker Client

A modern, responsive Single Page Application (SPA) built with **Angular 21** and **Tailwind CSS 4**. This frontend serves as the user interface for the Finance Tracker API, providing a seamless experience for managing personal finances, visualizing data, and handling secure authentication.

## 🚀 Tech Stack

This project uses the latest web technologies for performance and developer experience:

*   **Framework:** [Angular v21](https://angular.dev/) (Latest)
*   **Styling:** [Tailwind CSS v4](https://tailwindcss.com/) (Utility-first CSS)
*   **State & Async:** RxJS
*   **Authentication:**
    *   JWT (JSON Web Tokens) handling via Interceptors
    *   Social Login integration (`@abacritt/angularx-social-login`)
    *   QR Code display for 2FA setup
*   **Build & Tooling:**
    *   Angular CLI
    *   PostCSS
    *   Docker & Nginx (for production serving)

## 🛠️ Core Features

The application is structured into feature-based components:

### 1. Authentication & Security
*   **Login/Register:** Secure forms with validation.
*   **Two-Factor Authentication (2FA):**
    *   Setup flow with QR Code generation.
    *   Verification flow for local and OAuth logins.
*   **Social Login:** One-click sign-in with Google.
*   **Password Management:** Forgot Password and Reset Password flows via email.

### 2. Dashboard
*   **Financial Summary:** Real-time view of Total Income, Total Expenses, and Net Balance.
*   **Visualizations:** Interactive charts showing financial trends over the last 6 months.
*   **Recent Activity:** Quick view of the latest transactions.

### 3. Transaction Management
*   **Expenses & Revenues:** Dedicated sections for managing money in and money out.
*   **CRUD Operations:** Create, Read, Update, and Delete transactions.
*   **Filtering:** Filter records by date range to analyze spending/earning habits.
*   **Responsive Tables:** Clean data presentation with action controls.

### 4. User Settings
*   **Profile Management:** View and update user details.
*   **Avatar:** Upload, update, or remove profile pictures.
*   **Account Controls:** Options to delete the account permanently.

## 📂 Project Structure

```text
src/app/
├── components/          # Feature-based UI components
│   ├── dashboard/       # Main overview with charts
│   ├── expense/         # Expense lists and forms
│   ├── revenue/         # Revenue lists and forms
│   ├── login/           # Auth forms (Login, Register, 2FA)
│   ├── settings/        # User profile settings
│   └── shared/          # Reusable components (Sidebar, etc.)
├── services/            # Business logic and API communication
│   ├── auth.ts          # Authentication state & methods
│   ├── data.ts          # Generic data service for Expenses/Revenues
│   ├── user.service.ts  # User profile management
│   └── token.ts         # JWT storage utility
├── guards/              # Route protection
│   └── auth.guard.ts    # Prevents unauthorized access
├── interceptors/        # HTTP handling
│   └── auth.interceptor.ts # Attaches JWT to requests automatically
└── config.ts            # Dynamic API URL configuration
```

## 💻 Getting Started

### Prerequisites
*   Node.js (v18+ recommended)
*   npm or yarn



