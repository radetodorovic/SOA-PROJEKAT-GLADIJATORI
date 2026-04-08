import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { UsersService } from '../../services/users.service';
import { UserAccount } from '../../models/user-account';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent implements OnInit {
  users: UserAccount[] = [];
  isLoading = true;
  errorMessage = '';
  actionMessage = '';
  currentAdminId: number | null = null;
  private readonly blockingUserIds = new Set<number>();

  constructor(private readonly usersService: UsersService) {}

  ngOnInit(): void {
    this.currentAdminId = this.readAdminIdFromStorage();
    this.loadUsers();
  }

  private loadUsers(): void {
    this.usersService.getAll().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesno ucitavanje korisnika.';
        this.isLoading = false;
      }
    });
  }

  blockUser(user: UserAccount): void {
    if (user.isBlocked || user.role === 'Admin' || this.currentAdminId === null || this.isBlocking(user.id)) {
      return;
    }

    this.actionMessage = '';
    this.errorMessage = '';
    this.blockingUserIds.add(user.id);

    this.usersService
      .blockUser(user.id, this.currentAdminId)
      .pipe(finalize(() => this.blockingUserIds.delete(user.id)))
      .subscribe({
        next: (updatedUser) => {
          this.users = this.users.map((item) => (item.id === updatedUser.id ? updatedUser : item));
          this.actionMessage = `Korisnik ${updatedUser.username} je uspesno blokiran.`;
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = error.error?.message ?? 'Blokiranje korisnika nije uspelo.';
        }
      });
  }

  isBlocking(userId: number): boolean {
    return this.blockingUserIds.has(userId);
  }

  private readAdminIdFromStorage(): number | null {
    const raw = localStorage.getItem('currentUser');
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw) as { id?: number; role?: string };
      if (parsed.role !== 'Admin' || typeof parsed.id !== 'number') {
        return null;
      }

      return parsed.id;
    } catch {
      return null;
    }
  }
}
