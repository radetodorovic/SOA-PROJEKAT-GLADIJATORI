import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegisterUserRequest } from '../models/register-user-request';
import { RegisteredUser } from '../models/registered-user';
import { LoginUserRequest } from '../models/login-user-request';
import { LoginResponse } from '../models/login-response';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly loginUrl = `${environment.apiBaseUrl}/auth/login`;
  private readonly registerUrl = `${environment.apiBaseUrl}/auth/register`;

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginUserRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.loginUrl, payload);
  }

  register(payload: RegisterUserRequest): Observable<RegisteredUser> {
    return this.http.post<RegisteredUser>(this.registerUrl, payload);
  }
}
