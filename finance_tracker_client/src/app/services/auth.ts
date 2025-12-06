import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_CONFIG } from '../config';
import { TokenService } from './token';

interface AuthResponse {
  message: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = API_CONFIG.apiUrl;

  constructor(private http: HttpClient, private tokenService: TokenService) { }

  public login(username: string, password: string): Observable<AuthResponse> {
    // Calls the Spring Boot /api/auth/login endpoint
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, { username, password })
      .pipe(
        tap(response => {
          this.tokenService.saveToken(response.token);
        })
      );
  }

  public loginWithGoogle(idToken: string): Observable<AuthResponse> {
    // Calls the Spring Boot /api/auth/oauth endpoint
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/oauth`, { 
      token: idToken,
      provider: 'google'
    }).pipe(
      tap(response => {
        this.tokenService.saveToken(response.token);
      })
    );
  }

  public register(username: string, password: string, email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, { username, password, email });
  }

  public forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, { email });
  }

  public resetPassword(email: string, token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/reset-password`, { email, token, newPassword });
  }

  public getToken(): string | null {
    return this.tokenService.getToken();
  }

  public isLoggedIn(): boolean {
    return !!this.getToken();
  }

  public logout(): void {
    this.tokenService.clearToken();
  }
}
