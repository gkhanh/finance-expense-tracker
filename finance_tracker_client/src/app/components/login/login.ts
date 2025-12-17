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
  twoFactorCode = '';
  loginStep: 'credentials' | 'setup' | 'verify' = 'credentials';
  setupData: any = null;
  qrCodeUrl = '';
  showSecret = false;  // Keep original property
  useEmailOtp = false;  // New property for email OTP
  emailOtpSent = false;  // New property
  emailOtpMessage = '';  // New property
  
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
      next: (response) => {
        this.loading = false;
        if (response.token) {
           this.router.navigate(['/dashboard']); 
        } else if (response.setup2fa) {
           this.loginStep = 'setup';
           this.setupData = response;
           this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(response.qrUrl!)}`;
           this.useEmailOtp = false;
           this.emailOtpSent = false;
           this.emailOtpMessage = '';
           this.twoFactorCode = '';
        } else if (response.requires2fa) {
           this.loginStep = 'verify';
           this.useEmailOtp = false;
           this.emailOtpSent = false;
           this.emailOtpMessage = '';
           this.twoFactorCode = '';
        }
      },
      error: (error) => {
        this.loading = false;
        if (error.status === 401 || error.status === 400) {
            // Check if the backend provided a specific message
            this.errorMessage = error.error?.message || 'The email or password you entered is incorrect.';
        } else if (error.status === 0) {
            this.errorMessage = 'Unable to connect to server. Please check your internet connection.';
        } else {
            this.errorMessage = 'An unexpected error occurred. Please try again later.';
        }
      }
    });
  }

  toggleSecret(): void {
    this.showSecret = !this.showSecret;
  }

  onCodeInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    // Remove any non-digit characters
    let value = input.value.replace(/\D/g, '');
    // Limit to 6 digits
    value = value.slice(0, 6);
    this.twoFactorCode = value;
    input.value = value;
    
    // Clear error message when user starts typing
    if (this.errorMessage && value.length > 0) {
      this.errorMessage = null;
    }
  }

  isValidCode(): boolean {
    return /^\d{6}$/.test(this.twoFactorCode);
  }

  sendEmailOtp(): void {
    this.loading = true;
    this.errorMessage = null;
    this.emailOtpMessage = '';

    this.authService.send2FAEmailOtp(this.username).subscribe({
      next: (response) => {
        this.loading = false;
        this.useEmailOtp = true;
        this.emailOtpSent = true;
        this.emailOtpMessage = response.message || 'An email with OTP code has been sent to your email address.';
      },
      error: (error) => {
        this.loading = false;
        if (error.status === 400) {
          this.errorMessage = error.error?.message || 'Failed to send email OTP. Please try again.';
        } else if (error.status === 0) {
          this.errorMessage = 'Unable to connect to server. Please check your internet connection.';
        } else {
          this.errorMessage = 'Failed to send email OTP. Please try again later.';
        }
      }
    });
  }

  onVerify2fa(): void {
      // Validate code format before sending
      if (!this.isValidCode()) {
          this.errorMessage = this.useEmailOtp 
            ? 'Please enter a valid 6-digit code from your email.' 
            : 'Please enter a valid 6-digit code from your authenticator app.';
          return;
      }

      this.loading = true;
      this.errorMessage = null;
      
      // Pass useEmailOtp flag - defaults to false for existing flow
      const verify$ = this.password ? 
        this.authService.verify2fa(this.username, this.twoFactorCode, this.password, this.useEmailOtp) :
        this.authService.verify2faOAuth(this.username, this.twoFactorCode, this.useEmailOtp);

      verify$.subscribe({
          next: (response) => {
              this.loading = false;
              if (response.token) {
                  this.router.navigate(['/dashboard']);
              }
          },
          error: (error) => {
              this.loading = false;
              if (error.status === 401 || error.status === 400) {
                  this.errorMessage = error.error?.message || 'Invalid code. Please try again.';
              } else if (error.status === 0) {
                  this.errorMessage = 'Unable to connect to server. Please check your internet connection.';
              } else {
                  this.errorMessage = 'An unexpected error occurred. Please try again later.';
              }
          }
      });
  }

  handleGoogleLogin(idToken: string): void {
    this.loading = true;
    this.authService.loginWithGoogle(idToken).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.token) {
           this.router.navigate(['/dashboard']);
        } else if (response.setup2fa) {
           this.username = response.username!; // Store username for step 2
           this.password = ''; // Clear password for oauth flow
           this.loginStep = 'setup';
           this.setupData = response;
           this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(response.qrUrl!)}`;
           this.useEmailOtp = false;
           this.emailOtpSent = false;
           this.emailOtpMessage = '';
           this.twoFactorCode = '';
        } else if (response.requires2fa) {
           this.username = response.username!; // Store username for step 3
           this.password = ''; // Clear password for oauth flow
           this.loginStep = 'verify';
           this.useEmailOtp = false;
           this.emailOtpSent = false;
           this.emailOtpMessage = '';
           this.twoFactorCode = '';
        }
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
