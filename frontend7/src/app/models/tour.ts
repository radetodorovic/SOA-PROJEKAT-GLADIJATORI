export interface KeyPoint {
  id: string;
  name: string;
  description: string | null;
  latitude: number;
  longitude: number;
  imageUrl: string | null;
  order: number;
}

export interface Tour {
  id: string;
  authorId: number;
  name: string;
  description: string;
  status: string;
  difficulty: string;
  price: number;
  distanceKm: number;
  transportDurations: Record<string, number>;
  keyPoints: KeyPoint[];
  tags: string[];
  createdAt: string;
  publishedAt: string | null;
  archivedAt: string | null;
}

export interface Review {
  id: string;
  tourId: string;
  userId: number;
  username: string;
  rating: number;
  comment: string | null;
  visitDate: string | null;
  imageUrls: string[];
  createdAt: string;
}

export interface TouristPosition {
  id: string;
  userId: number;
  latitude: number;
  longitude: number;
  updatedAt: string;
}

export interface KeyPointRequest {
  name: string;
  description: string | null;
  latitude: number;
  longitude: number;
  imageUrl: string | null;
  order: number;
}

export interface OrderItem {
  tourId: string;
  tourName: string;
  price: number;
}

export interface ShoppingCart {
  touristId: number;
  items: OrderItem[];
  totalPrice: number;
}

export interface TourPurchaseToken {
  tourId: string;
  token: string;
  createdAt: string;
}

export interface CompletedKeyPoint {
  keyPointId: string;
  keyPointName: string;
  reachedAt: string;
}

export interface TourExecution {
  id: string;
  tourId: string;
  touristId: number;
  status: string;
  startLatitude: number;
  startLongitude: number;
  startedAt: string;
  completedAt: string | null;
  abandonedAt: string | null;
  lastActivity: string;
  completedKeyPoints: CompletedKeyPoint[];
}
