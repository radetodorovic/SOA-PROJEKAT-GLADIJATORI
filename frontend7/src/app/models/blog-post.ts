export interface BlogPost {
  id: number;
  authorId: number;
  title: string;
  description: string;
  createdAtUtc: string;
  descriptionFormat: string;
  images: string[];
  likesCount?: number;
  isLikedByCurrentUser?: boolean;
}
