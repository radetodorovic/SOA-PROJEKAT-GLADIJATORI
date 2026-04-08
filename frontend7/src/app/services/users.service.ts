import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserAccount } from '../models/user-account';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private readonly usersUrl = `${environment.apiBaseUrl}/users`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<UserAccount[]> {
    return this.http.get<UserAccount[]>(this.usersUrl);
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
}
