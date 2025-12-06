import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms'; 
import { CommonModule } from '@angular/common'; 
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { SocialAuthService, GoogleLoginProvider, SocialUser, GoogleSigninButtonModule } from '@abacritt/angularx-social-login';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, GoogleSigninButtonModule], 
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {
  username = ''; 
  password = ''; 
  errorMessage: string | null = null;
  loading = false;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private socialAuthService: SocialAuthService
  ) { }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
      return;
    }

    // Subscribe to social auth state
    this.socialAuthService.authState.subscribe((user: SocialUser) => {
      if (user && user.idToken) {
        this.handleGoogleLogin(user.idToken);
      }
    });
  }

  onLogin(): void {
    this.errorMessage = null;
    this.loading = true;

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']); 
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = (error.status === 401 || error.status === 400) 
                            ? 'The email or password you entered is incorrect.' 
                            : 'Unable to connect to server. Please try again later.';
      }
    });
  }

  handleGoogleLogin(idToken: string): void {
    this.loading = true;
    this.authService.loginWithGoogle(idToken).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.loading = false;
        if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = 'Google Login Failed. Please try again.';
        }
      }
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  socialLogin(provider: string): void {
    if (provider === 'Google') {
      this.socialAuthService.signIn(GoogleLoginProvider.PROVIDER_ID);
    } else {
      alert(`${provider} login is not yet configured.`);
    }
  }

  forgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }
}
