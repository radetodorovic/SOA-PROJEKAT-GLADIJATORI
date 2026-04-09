import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

type AppRole = 'Admin' | 'Guide' | 'Tourist';

interface CurrentUser {
  id: number;
  username: string;
  email: string;
  role: AppRole;
  isBlocked: boolean;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  currentUser: CurrentUser | null = null;
  isAuthRoute = false;

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.syncSessionState();
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => this.syncSessionState());
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'Admin';
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUser = null;
    this.router.navigate(['/auth']);
  }

  private syncSessionState(): void {
    this.currentUser = this.readCurrentUser();
    this.isAuthRoute = this.router.url.startsWith('/auth');
  }

  private readCurrentUser(): CurrentUser | null {
    const raw = localStorage.getItem('currentUser');
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw) as CurrentUser;
      if (typeof parsed.id !== 'number' || !parsed.role) {
        return null;
      }

      return parsed;
    } catch {
      return null;
    }
  }
}
