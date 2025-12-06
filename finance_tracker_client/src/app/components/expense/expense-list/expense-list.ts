import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { DataService, Expense } from '../../../services/data';
import { SidebarComponent } from '../../shared/sidebar';

@Component({
  selector: 'app-expense-list',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, SidebarComponent],
  templateUrl: './expense-list.html',
  styleUrls: ['../../dashboard/dashboard.css', './expense-list.css'] // Reuse dashboard styles
})
export class ExpenseListComponent implements OnInit {
  expenses: Expense[] = [];
  loading = true;
  error: string | null = null;

  constructor(private dataService: DataService, private router: Router) {}

  ngOnInit(): void {
    this.loadExpenses();
  }

  loadExpenses(): void {
    this.loading = true;
    this.dataService.getExpenses().subscribe({
      next: (data) => {
        this.expenses = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load expenses.';
        this.loading = false;
      }
    });
  }

  goToAdd(): void {
    this.router.navigate(['/expenses/add']);
  }

  editExpense(id: string): void {
    this.router.navigate(['/expenses/edit', id]);
  }

  deleteExpense(id: string): void {
    if (confirm('Are you sure you want to delete this expense?')) {
      this.dataService.deleteExpense(id).subscribe(() => {
        this.expenses = this.expenses.filter(e => e.id !== id);
      });
    }
  }
}

