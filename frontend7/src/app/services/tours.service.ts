import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Tour, Review, TouristPosition, KeyPointRequest, ShoppingCart, TourPurchaseToken, TourExecution } from '../models/tour';

export interface UpdateTourRequest {
  name?: string;
  description?: string;
  price?: number;
  difficulty?: string;
  status?: string;
  transportDurations?: Record<string, number>;
  tags?: string[];
}

@Injectable({ providedIn: 'root' })
export class ToursService {
  private readonly toursUrl = `${environment.tourApiBaseUrl}/tours`;
  private readonly sagasUrl = `${environment.tourApiBaseUrl}/sagas`;

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

  getPurchasedTours(userId: number): Observable<Tour[]> {
    return this.http.get<Tour[]>(`${this.toursUrl}/purchased`, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  getTourById(tourId: string, userId: number): Observable<Tour> {
    return this.http.get<Tour>(`${this.toursUrl}/${tourId}`, {
      headers: { 'X-User-Id': String(userId) }
    });
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

  getCart(userId: number): Observable<ShoppingCart> {
    return this.http.get<ShoppingCart>(`${this.toursUrl}/cart`, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  addToCart(userId: number, tourId: string): Observable<ShoppingCart> {
    return this.http.post<ShoppingCart>(`${this.toursUrl}/cart/items/${tourId}`, {}, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  removeFromCart(userId: number, tourId: string): Observable<ShoppingCart> {
    return this.http.delete<ShoppingCart>(`${this.toursUrl}/cart/items/${tourId}`, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  checkout(userId: number): Observable<TourPurchaseToken[]> {
    return this.http.post<{ payload: TourPurchaseToken[] }>(`${this.sagasUrl}/checkout`, {}, {
      headers: { 'X-User-Id': String(userId) }
    }).pipe(map(response => response.payload));
  }

  getPurchaseTokens(userId: number): Observable<TourPurchaseToken[]> {
    return this.http.get<TourPurchaseToken[]>(`${this.toursUrl}/purchases`, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  startExecution(userId: number, tourId: string): Observable<TourExecution> {
    return this.http.post<{ payload: TourExecution }>(`${this.sagasUrl}/tours/${tourId}/execution/start`, {}, {
      headers: { 'X-User-Id': String(userId) }
    }).pipe(map(response => response.payload));
  }

  getActiveExecution(userId: number, tourId: string): Observable<TourExecution | null> {
    return this.http.get<TourExecution | null>(`${this.toursUrl}/${tourId}/execution/active`, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  checkExecution(userId: number, tourId: string): Observable<TourExecution> {
    return this.http.post<TourExecution>(`${this.toursUrl}/${tourId}/execution/check`, {}, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  completeExecution(userId: number, tourId: string): Observable<TourExecution> {
    return this.http.post<TourExecution>(`${this.toursUrl}/${tourId}/execution/complete`, {}, {
      headers: { 'X-User-Id': String(userId) }
    });
  }

  abandonExecution(userId: number, tourId: string): Observable<TourExecution> {
    return this.http.post<TourExecution>(`${this.toursUrl}/${tourId}/execution/abandon`, {}, {
      headers: { 'X-User-Id': String(userId) }
    });
  }
}
