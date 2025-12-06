import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DataService, Revenue } from '../../../services/data';
import { SidebarComponent } from '../../shared/sidebar';

@Component({
  selector: 'app-revenue-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './revenue-form.html',
  styleUrls: ['../../dashboard/dashboard.css', '../../expense/expense-form/expense-form.css'] // Reuse existing styles
})
export class RevenueFormComponent implements OnInit {
  revenue: Revenue = {
    source: '',
    amount: 0,
    date: new Date().toISOString().split('T')[0]
  };
  
  isEditMode = false;
  loading = false;
  error: string | null = null;

  constructor(
    private dataService: DataService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loadRevenue(id);
    }
  }

  loadRevenue(id: string): void {
    this.loading = true;
    this.dataService.getRevenue(id).subscribe({
      next: (data) => {
        this.revenue = data;
        if (this.revenue.date) {
            this.revenue.date = new Date(this.revenue.date).toISOString().split('T')[0];
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load revenue details.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.error = null;

    if (this.isEditMode && this.revenue.id) {
      this.dataService.updateRevenue(this.revenue.id, this.revenue).subscribe({
        next: () => this.router.navigate(['/revenues']),
        error: (err) => {
          this.error = 'Failed to update revenue.';
          this.loading = false;
        }
      });
    } else {
      this.dataService.createRevenue(this.revenue).subscribe({
        next: () => this.router.navigate(['/revenues']),
        error: (err) => {
          this.error = 'Failed to create revenue.';
          this.loading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/revenues']);
  }
}

