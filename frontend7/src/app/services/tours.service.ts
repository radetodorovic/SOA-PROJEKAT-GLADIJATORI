import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Tour, Review, TouristPosition, KeyPointRequest } from '../models/tour';

export interface UpdateTourRequest {
  name?: string;
  description?: string;
  price?: number;
  difficulty?: string;
  status?: string;
  tags?: string[];
}

@Injectable({ providedIn: 'root' })
export class ToursService {
  private readonly toursUrl = `${environment.tourApiBaseUrl}/tours`;

  constructor(private readonly http: HttpClient) {}

  createTour(userId: number, name: string, description: string, price: number, difficulty: string, tags: string[]): Observable<Tour> {
    return this.http.post<Tour>(this.toursUrl, { name, description, price, difficulty, tags }, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  updateTour(tourId: string, userId: number, data: UpdateTourRequest): Observable<Tour> {
    return this.http.put<Tour>(`${this.toursUrl}/${tourId}`, data, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  getPublishedTours(): Observable<Tour[]> {
    return this.http.get<Tour[]>(`${this.toursUrl}/published`);
  }

  getToursByAuthor(authorId: number): Observable<Tour[]> {
    return this.http.get<Tour[]>(`${this.toursUrl}/author/${authorId}`);
  }

  addKeyPoint(tourId: string, userId: number, kp: KeyPointRequest): Observable<Tour> {
    return this.http.post<Tour>(`${this.toursUrl}/${tourId}/keypoints`, kp, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  updateKeyPoint(tourId: string, keypointId: string, userId: number, kp: KeyPointRequest): Observable<Tour> {
    return this.http.put<Tour>(`${this.toursUrl}/${tourId}/keypoints/${keypointId}`, kp, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  deleteKeyPoint(tourId: string, keypointId: string, userId: number): Observable<Tour> {
    return this.http.delete<Tour>(`${this.toursUrl}/${tourId}/keypoints/${keypointId}`, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  addReview(
    tourId: string,
    userId: number,
    username: string,
    rating: number,
    comment: string,
    visitDate: string | null,
    imageUrls: string[]
  ): Observable<Review> {
    return this.http.post<Review>(`${this.toursUrl}/${tourId}/reviews`,
      { rating, comment, username, visitDate: visitDate || null, imageUrls },
      { headers: { 'X-User-Id': String(userId) } }
    );
  }

  getReviews(tourId: string): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.toursUrl}/${tourId}/reviews`);
  }

  updatePosition(userId: number, latitude: number, longitude: number): Observable<void> {
    return this.http.put<void>(`${this.toursUrl}/position`, { latitude, longitude }, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  getPosition(userId: number): Observable<TouristPosition> {
    return this.http.get<TouristPosition>(`${this.toursUrl}/position/${userId}`);
  }
}
