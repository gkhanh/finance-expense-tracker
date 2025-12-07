import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { DashboardComponent } from './components/dashboard/dashboard';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password';
import { ExpenseListComponent } from './components/expense/expense-list/expense-list';
import { ExpenseFormComponent } from './components/expense/expense-form/expense-form';
import { RevenueListComponent } from './components/revenue/revenue-list/revenue-list';
import { RevenueFormComponent } from './components/revenue/revenue-form/revenue-form';
import { SettingsComponent } from './components/settings/settings';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Auth Routes
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },

  // Protected Routes
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [authGuard] },
  
  // Expenses
  { path: 'expenses', component: ExpenseListComponent, canActivate: [authGuard] },
  { path: 'expenses/add', component: ExpenseFormComponent, canActivate: [authGuard] },
  { path: 'expenses/edit/:id', component: ExpenseFormComponent, canActivate: [authGuard] },

  // Revenues
  { path: 'revenues', component: RevenueListComponent, canActivate: [authGuard] },
  { path: 'revenues/add', component: RevenueFormComponent, canActivate: [authGuard] },
  { path: 'revenues/edit/:id', component: RevenueFormComponent, canActivate: [authGuard] },

  // Fallback
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
