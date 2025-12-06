import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  private tokenKey = 'jwtToken';

  public getToken(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }

  public saveToken(token: string): void {
    sessionStorage.setItem(this.tokenKey, token);
  }

  public clearToken(): void {
    sessionStorage.removeItem(this.tokenKey);
  }
}
