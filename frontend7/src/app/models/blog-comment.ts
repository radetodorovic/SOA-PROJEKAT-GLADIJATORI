export interface BlogComment {
  id: number;
  blogId: number;
  userId: number;
  text: string;
  createdAtUtc: string;
  updatedAtUtc: string;
}
