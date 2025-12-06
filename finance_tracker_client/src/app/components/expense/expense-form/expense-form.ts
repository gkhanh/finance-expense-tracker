import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DataService, Expense } from '../../../services/data';
import { SidebarComponent } from '../../shared/sidebar';

@Component({
  selector: 'app-expense-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './expense-form.html',
  styleUrls: ['../../dashboard/dashboard.css', './expense-form.css']
})
export class ExpenseFormComponent implements OnInit {
  expense: Expense = {
    description: '',
    amount: 0,
    category: '',
    date: new Date().toISOString().split('T')[0]
  };
  
  isEditMode = false;
  loading = false;
  error: string | null = null;
  
  categories = ['Food', 'Rent', 'Utilities', 'Transportation', 'Entertainment', 'Healthcare', 'Other'];

  constructor(
    private dataService: DataService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadExpense(id);
    }
  }

  loadExpense(id: string): void {
    this.loading = true;
    this.dataService.getExpense(id).subscribe({
      next: (data) => {
        this.expense = data;
        // Ensure date is formatted for input type="date"
        if (this.expense.date) {
            this.expense.date = new Date(this.expense.date).toISOString().split('T')[0];
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load expense details.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.error = null;

    if (this.isEditMode && this.expense.id) {
      this.dataService.updateExpense(this.expense.id, this.expense).subscribe({
        next: () => this.router.navigate(['/expenses']),
        error: (err) => {
          this.error = 'Failed to update expense.';
          this.loading = false;
        }
      });
    } else {
      this.dataService.createExpense(this.expense).subscribe({
        next: () => this.router.navigate(['/expenses']),
        error: (err) => {
          this.error = 'Failed to create expense.';
          this.loading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/expenses']);
  }
}

