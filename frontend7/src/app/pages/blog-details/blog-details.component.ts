import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { BlogsService } from '../../services/blogs.service';
import { BlogPost } from '../../models/blog-post';

type AppRole = 'Admin' | 'Guide' | 'Tourist';

interface CurrentUser {
  id: number;
  username: string;
  email: string;
  role: AppRole;
  isBlocked: boolean;
}

@Component({
  selector: 'app-blog-details',
  templateUrl: './blog-details.component.html',
  styleUrls: ['./blog-details.component.css']
})
export class BlogDetailsComponent implements OnInit {
  blog: BlogPost | null = null;
  isLoading = true;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly blogsService: BlogsService
  ) {}

  ngOnInit(): void {
    const user = this.readCurrentUser();
    if (!user) {
      this.router.navigate(['/auth']);
      return;
    }

    this.route.paramMap.subscribe((params) => {
      const blogId = Number(params.get('id'));
      if (!Number.isInteger(blogId) || blogId <= 0) {
        this.errorMessage = 'Neispravan id bloga.';
        this.blog = null;
        this.isLoading = false;
        return;
      }

      this.loadBlog(blogId);
    });
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString('sr-RS');
  }

  private loadBlog(blogId: number): void {
    this.isLoading = true;
    this.blogsService.getBlogById(blogId).subscribe({
      next: (blog) => {
        this.blog = blog;
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesno ucitavanje bloga.';
        this.blog = null;
        this.isLoading = false;
      }
    });
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
