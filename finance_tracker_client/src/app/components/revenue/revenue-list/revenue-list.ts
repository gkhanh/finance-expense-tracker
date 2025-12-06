import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { DataService, Revenue } from '../../../services/data';
import { SidebarComponent } from '../../shared/sidebar';

@Component({
  selector: 'app-revenue-list',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, SidebarComponent],
  templateUrl: './revenue-list.html',
  styleUrls: ['../../dashboard/dashboard.css', '../../expense/expense-list/expense-list.css'] // Reuse existing styles
})
export class RevenueListComponent implements OnInit {
  revenues: Revenue[] = [];
  loading = true;
  error: string | null = null;

  constructor(private dataService: DataService, private router: Router) {}

  ngOnInit(): void {
    this.loadRevenues();
  }

  loadRevenues(): void {
    this.loading = true;
    this.dataService.getRevenues().subscribe({
      next: (data) => {
        this.revenues = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load revenues.';
        this.loading = false;
      }
    });
  }

  goToAdd(): void {
    this.router.navigate(['/revenues/add']);
  }

  editRevenue(id: string): void {
    this.router.navigate(['/revenues/edit', id]);
  }

  deleteRevenue(id: string): void {
    if (confirm('Are you sure you want to delete this revenue record?')) {
      this.dataService.deleteRevenue(id).subscribe(() => {
        this.revenues = this.revenues.filter(r => r.id !== id);
      });
    }
  }
}

