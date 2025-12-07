import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../config';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = API_CONFIG.apiUrl;

  constructor(private http: HttpClient) { }

  updateAvatar(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/users/avatar`, formData);
  }

  deleteAvatar(): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/avatar`);
  }

  getCurrentUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/me`);
  }

  deleteAccount(): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/me`);
  }
}
