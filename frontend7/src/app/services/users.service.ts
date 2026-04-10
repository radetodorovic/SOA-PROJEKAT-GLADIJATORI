import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserAccount } from '../models/user-account';
import { UserProfile } from '../models/user-profile';
import { InitializeUserProfileRequest } from '../models/initialize-user-profile-request';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private readonly usersUrl = `${environment.apiBaseUrl}/users`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(this.usersUrl);
  }

  getUserById(userId: number): Observable<UserAccount> {
    return this.http.get<UserAccount>(`${this.usersUrl}/${userId}`);
  }

  blockUser(userId: number, adminId: number): Observable<UserAccount> {
    return this.http.patch<UserAccount>(
      `${this.usersUrl}/${userId}/block`,
      {},
      {
        headers: {
          'X-Admin-Id': String(adminId)
        }
      }
    );
  }

  getMyProfile(userId: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.usersUrl}/${userId}/profile`, {
      headers: {
        'X-User-Id': String(userId)
      }
    });
  }

  initializeMyProfile(userId: number, payload: InitializeUserProfileRequest): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.usersUrl}/${userId}/profile-initialization`, payload, {
      headers: {
        'X-User-Id': String(userId)
      }
    });
  }

  updateMyProfile(userId: number, payload: InitializeUserProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.usersUrl}/${userId}/profile`, payload, {
      headers: {
        'X-User-Id': String(userId)
      }
    });
  }
}
