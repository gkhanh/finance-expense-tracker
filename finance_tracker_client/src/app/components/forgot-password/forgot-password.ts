import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPasswordComponent {
  email = '';
  token = '';
  newPassword = '';
  
  emailSent = false;
  loading = false;
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.loading = true;
    this.errorMessage = null;

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.emailSent = true;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to send reset code.';
      }
    });
  }

  onReset(): void {
    const passwordRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{10,}$/;
    if (!passwordRegex.test(this.newPassword)) {
      this.errorMessage = 'Password must be at least 10 characters long and contain at least one digit, one lowercase letter, one uppercase letter, and one special character.';
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    this.authService.resetPassword(this.email, this.token, this.newPassword).subscribe({
      next: () => {
        this.loading = false;
        alert('Password reset successful! You can now login.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to reset password.';
      }
    });
  }

  backToLogin(): void {
    this.router.navigate(['/login']);
  }
}
