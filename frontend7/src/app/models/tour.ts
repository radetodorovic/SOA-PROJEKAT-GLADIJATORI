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
  keyPoints: KeyPoint[];
  tags: string[];
  createdAt: string;
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
