import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { SocialAuthService } from '@abacritt/angularx-social-login';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="sidebar">
        <div class="logo">FINANCE<span>PRO</span></div>
        <ul>
            <li routerLink="/dashboard" routerLinkActive="active">Overview</li>
            <li routerLink="/expenses" routerLinkActive="active">Expenses</li>
            <li routerLink="/revenues" routerLinkActive="active">Revenues</li>
            <li (click)="onLogout()" class="logout-item">Logout</li>
        </ul>
    </nav>
  `,
  styles: [`
    /* Sidebar Glass - Fixed and Rounded */
    .sidebar {
        width: 260px;
        height: 94vh; /* Slightly less than full height */
        position: fixed; /* Fix position */
        top: 3vh; /* Center vertically */
        left: 20px; /* Gap from edge */
        background: rgba(30, 41, 59, 0.4);
        backdrop-filter: blur(15px);
        -webkit-backdrop-filter: blur(15px);
        border: 1px solid rgba(255, 255, 255, 0.08); /* Full border */
        border-radius: 20px; /* Rounded corners */
        padding: 25px;
        display: flex;
        flex-direction: column;
        box-sizing: border-box;
        z-index: 50; /* Ensure it stays on top */
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }

    .logo {
        font-size: 1.6rem;
        font-weight: 800;
        color: white;
        margin-bottom: 50px;
        letter-spacing: -0.5px;
        background: linear-gradient(to right, #fff, #94a3b8);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        cursor: default;
    }

    .logo span { color: #3b82f6; -webkit-text-fill-color: #3b82f6; }

    ul { list-style: none; padding: 0; margin: 0; }
    li {
        padding: 14px 18px;
        margin-bottom: 8px;
        border-radius: 12px;
        color: #94a3b8;
        cursor: pointer;
        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        font-weight: 500;
        display: flex;
        align-items: center;
        text-decoration: none;
    }

    li:hover {
        background-color: rgba(255, 255, 255, 0.05);
        color: #e2e8f0;
        transform: translateX(4px);
    }

    li.active {
        background: linear-gradient(90deg, rgba(59, 130, 246, 0.2) 0%, rgba(59, 130, 246, 0.05) 100%);
        color: #60a5fa;
        border-left: 3px solid #60a5fa;
    }

    .logout-item {
        margin-top: auto;
        color: #ef4444 !important;
    }
    .logout-item:hover {
        background: rgba(239, 68, 68, 0.1) !important;
    }
    @media (max-width: 1100px) {
        .sidebar { display: none; }
    }
  `]
})
export class SidebarComponent {
  constructor(
    private authService: AuthService,
    private router: Router,
    private socialAuthService: SocialAuthService
  ) {}

  onLogout(): void {
    this.authService.logout();
    this.socialAuthService.signOut().catch(() => {});
    this.router.navigate(['/login']);
  }
}

