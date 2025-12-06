import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config';

export interface DashboardSummary {
  netBalance: number;
  monthlyIncome: number;
  incomeTrend: number;
  monthlyExpense: number;
  expenseTrend: number;
  monthName: string; // Added month name
}

export interface TrendPoint {
  month: string;
  income: number;
  expense: number;
}

export interface CategoryBreakdown { 
  [category: string]: number; 
}

export interface Expense {
  id?: string;
  description: string;
  amount: number;
  category: string;
  date: string; // ISO Date string
}

export interface Revenue {
  id?: string;
  source: string;
  amount: number;
  date: string; // ISO Date string
}

@Injectable({
  providedIn: 'root'
})

export class DataService {
  private apiUrl = API_CONFIG.apiUrl;
  constructor(private http: HttpClient) { }

  // Reports
  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/reports/summary`);
  }

  getTrend(): Observable<TrendPoint[]> {
    return this.http.get<TrendPoint[]>(`${this.apiUrl}/reports/trend`);
  }

  getCategoryBreakdown(): Observable<CategoryBreakdown> {
    return this.http.get<CategoryBreakdown>(`${this.apiUrl}/reports/breakdown`);
  }

  // Expenses
  getExpenses(startDate?: string, endDate?: string): Observable<Expense[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<Expense[]>(`${this.apiUrl}/expenses`, { params });
  }

  getExpense(id: string): Observable<Expense> {
    return this.http.get<Expense>(`${this.apiUrl}/expenses/${id}`);
  }

  createExpense(expense: Expense): Observable<Expense> {
    return this.http.post<Expense>(`${this.apiUrl}/expenses`, expense);
  }

  updateExpense(id: string, expense: Expense): Observable<Expense> {
    return this.http.put<Expense>(`${this.apiUrl}/expenses/${id}`, expense);
  }

  deleteExpense(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/expenses/${id}`);
  }

  // Revenues
  getRevenues(startDate?: string, endDate?: string): Observable<Revenue[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<Revenue[]>(`${this.apiUrl}/revenues`, { params });
  }

  getRevenue(id: string): Observable<Revenue> {
    return this.http.get<Revenue>(`${this.apiUrl}/revenues/${id}`);
  }

  createRevenue(revenue: Revenue): Observable<Revenue> {
    return this.http.post<Revenue>(`${this.apiUrl}/revenues`, revenue);
  }

  updateRevenue(id: string, revenue: Revenue): Observable<Revenue> {
    return this.http.put<Revenue>(`${this.apiUrl}/revenues/${id}`, revenue);
  }

  deleteRevenue(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/revenues/${id}`);
  }
}
