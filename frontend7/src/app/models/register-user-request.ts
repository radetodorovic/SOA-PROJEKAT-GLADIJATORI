export interface RegisterUserRequest {
  username: string;
  email: string;
  password: string;
  role: 'Guide' | 'Tourist';
}
