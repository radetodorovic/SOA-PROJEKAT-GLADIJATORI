import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FollowerUser } from '../models/follower-user';

interface UsersResponse {
  users: FollowerUser[];
}

@Injectable({
  providedIn: 'root'
})
export class FollowersService {
  private readonly followersUrl = `${environment.followerApiBaseUrl}/followers`;

  constructor(private readonly http: HttpClient) {}

  follow(currentUserId: number, targetUserId: number, currentUsername: string, targetUsername: string): Observable<void> {
    return this.http.post<void>(
      `${this.followersUrl}/${targetUserId}`,
      {
        followerUsername: currentUsername,
        targetUsername
      },
      {
        headers: {
          'X-User-Id': String(currentUserId)
        }
      }
    );
  }

  unfollow(currentUserId: number, targetUserId: number): Observable<void> {
    return this.http.delete<void>(`${this.followersUrl}/${targetUserId}`, {
      headers: {
        'X-User-Id': String(currentUserId)
      }
    });
  }

  getFollowing(userId: number): Observable<UsersResponse> {
    return this.http.get<UsersResponse>(`${this.followersUrl}/following/${userId}`);
  }

  getRecommendations(userId: number): Observable<UsersResponse> {
    return this.http.get<UsersResponse>(`${this.followersUrl}/recommendations/${userId}`);
  }
}
