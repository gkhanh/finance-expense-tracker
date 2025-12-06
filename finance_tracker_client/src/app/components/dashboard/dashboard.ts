import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DecimalPipe } from '@angular/common'; 
import { Router } from '@angular/router';
import { DataService, DashboardSummary, TrendPoint } from '../../services/data'; 
import { AuthService } from '../../services/auth';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import { catchError, finalize, of } from 'rxjs';
import { SidebarComponent } from '../shared/sidebar';

interface CategoryBreakdown { [category: string]: number; }

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DecimalPipe, SidebarComponent], 
  templateUrl: './dashboard.html', 
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  currentUser: string = 'Customer'; 
  summary: DashboardSummary | null = null;
  categoryBreakdown: CategoryBreakdown | null = null;
  trendData: TrendPoint[] = [];
  loading = true;
  error: string | null = null;

  pieChartData: { name: string, value: number, percentage: number }[] = [];
  
  hoveredTrendPoint: { x: number, y: number, income: number, expense: number, month: string } | null = null;
  hoveredPieSegment: { name: string, value: number, percentage: number } | null = null;
  pieTooltipPosition = { x: 50, y: 50 };

  constructor(
    private dataService: DataService, 
    private authService: AuthService,
    private router: Router,
    private socialAuthService: SocialAuthService
  ) { }

  ngOnInit(): void {
    this.extractUserFromToken();
    this.fetchData();
  }

  extractUserFromToken(): void {
    const token = this.authService.getToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUser = payload.sub || 'Customer';
      } catch (e) {
        console.error('Error decoding token name', e);
      }
    }
  }

  fetchData(): void {
    this.loading = true;
    
    // Fetch Summary
    this.dataService.getDashboardSummary().pipe(
      catchError(err => {
        this.handleError(err);
        return of(null);
      })
    ).subscribe(data => this.summary = data);

    // Fetch Trend Data
    this.dataService.getTrend().pipe(
      catchError(err => of([]))
    ).subscribe(data => this.trendData = data);

    // Fetch Category Breakdown
    this.dataService.getCategoryBreakdown().pipe(
      catchError(err => of(null)), 
      finalize(() => this.loading = false)
    ).subscribe(data => {
      this.categoryBreakdown = data;
      this.processChartData(data);
    });
  }

  private processChartData(data: CategoryBreakdown | null): void {
    if (!data) return;
    const total = Object.values(data).reduce((sum, value) => sum + value, 0);
    this.pieChartData = Object.entries(data).map(([name, value]) => ({
      name,
      value,
      percentage: total > 0 ? (value / total) * 100 : 0
    })).sort((a, b) => b.value - a.value);
  }

  private handleError(error: any): void {
    if (error.status === 401) {
      this.onLogout();
    } else {
      this.error = 'System unavailable. Please contact IT support.';
    }
  }

  getChartColor(index: number): string {
    const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];
    return colors[index % colors.length];
  }

  getDashOffset(index: number): number {
    let offset = 0; 
    for (let i = 0; i < index; i++) {
        offset -= this.pieChartData[i].percentage;
    }
    return offset;
  }

  onLogout(): void {
    this.authService.logout();
    this.socialAuthService.signOut().catch(err => console.log('Social signout check:', err));
    this.router.navigate(['/login']);
  }

  getTrendClass(trend?: number): string {
    if (trend === undefined || trend === 0) return 'neutral';
    return trend > 0 ? 'positive' : 'negative';
  }

  getTrendIcon(trend?: number): string {
    if (trend === undefined || trend === 0) return '─';
    return trend > 0 ? '▲' : '▼';
  }

  getAbsValue(value?: number): number {
    return value ? Math.abs(value) : 0;
  }

  // Trend Chart Helpers
  getChartRange(): number {
    if (!this.trendData || this.trendData.length === 0) return 100;
    const maxVal = Math.max(
        ...this.trendData.map(d => d.income), 
        ...this.trendData.map(d => d.expense),
        0
    );
    const minVal = Math.min(
        ...this.trendData.map(d => d.income), 
        ...this.trendData.map(d => d.expense),
        0
    );
    const boundary = Math.max(Math.abs(maxVal), Math.abs(minVal), 100);
    return Math.ceil(boundary / 10) * 10;
  }

  getPointX(index: number): number {
    if (this.trendData.length === 0) return 0;
    const width = 100;
    const step = width / (this.trendData.length - 1 || 1);
    return index * step;
  }

  getPointY(value: number): number {
    const range = this.getChartRange();
    const height = 100;
    // y = 50 - (val / range) * 50
    //const y = 50 - (val / range) * 50;
    // That logic allows negative values to go below 50. Correct.
    return 50 - (value / range) * 50;
  }

  getLinePath(type: 'income' | 'expense'): string {
    if (this.trendData.length === 0) return '';
    const range = this.getChartRange();
    const width = 100;
    const height = 100;
    const step = width / (this.trendData.length - 1 || 1);

    return this.trendData.map((d, i) => {
        const val = type === 'income' ? d.income : d.expense;
        const x = i * step;
        const y = 50 - (val / range) * 50;
        return `${i === 0 ? 'M' : 'L'} ${x},${y}`;
    }).join(' ');
  }

  // Interaction Handlers
  onTrendPointHover(point: TrendPoint, index: number, event: MouseEvent): void {
    this.hoveredTrendPoint = {
        x: this.getPointX(index),
        y: 50, // Center vertically or follow point? Let's follow mouse or fixed
        income: point.income,
        expense: point.expense,
        month: point.month
    };
  }

  onTrendPointLeave(): void {
    this.hoveredTrendPoint = null;
  }

  onPieHover(item: { name: string, value: number, percentage: number }, index: number): void {
    this.hoveredPieSegment = item;

    // Calculate position around the circle
    let cumulative = 0;
    for (let i = 0; i < index; i++) {
        cumulative += this.pieChartData[i].percentage;
    }
    const midPercent = cumulative + item.percentage / 2;
    
    const angleRad = (midPercent / 100) * 2 * Math.PI;
    const radius = 65; 
    
    const x = 50 + Math.sin(angleRad) * radius;
    const y = 50 - Math.cos(angleRad) * radius;
    
    this.pieTooltipPosition = { x, y };
  }

  onPieLeave(): void {
    this.hoveredPieSegment = null;
  }
}
