export interface LoginResponse {
  id: number;
  username: string;
  email: string;
  role: string;
  isBlocked: boolean;
  message: string;
}
