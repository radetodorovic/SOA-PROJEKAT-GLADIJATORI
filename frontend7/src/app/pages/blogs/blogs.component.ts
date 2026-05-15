import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { BlogsService } from '../../services/blogs.service';
import { FollowersService } from '../../services/followers.service';
import { UsersService } from '../../services/users.service';
import { BlogPost } from '../../models/blog-post';
import { CreateBlogRequest } from '../../models/create-blog-request';
import { UserAccount } from '../../models/user-account';
import { FollowerUser } from '../../models/follower-user';

type AppRole = 'Admin' | 'Guide' | 'Tourist';

interface CurrentUser {
  id: number;
  username: string;
  email: string;
  role: AppRole;
  isBlocked: boolean;
}

@Component({
  selector: 'app-blogs',
  templateUrl: './blogs.component.html',
  styleUrls: ['./blogs.component.css']
})
export class BlogsComponent implements OnInit {
  currentUser: CurrentUser | null = null;
  blogs: BlogPost[] = [];
  users: UserAccount[] = [];
  following: FollowerUser[] = [];
  recommendations: FollowerUser[] = [];

  titleInput = '';
  descriptionInput = '';
  selectedImages: string[] = [];

  isLoadingBlogs = true;
  isLoadingFollowers = false;
  isSavingBlog = false;
  updatingFollowUserId: number | null = null;

  errorMessage = '';
  infoMessage = '';
  followersErrorMessage = '';

  constructor(
    private readonly router: Router,
    private readonly blogsService: BlogsService,
    private readonly followersService: FollowersService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    const user = this.readCurrentUser();
    if (!user) {
      this.router.navigate(['/auth']);
      return;
    }

    this.currentUser = user;
    this.loadUsers();
    this.loadFollowerData();
    this.loadBlogs();
  }

  loadBlogs(): void {
    if (!this.currentUser) {
      return;
    }

    this.isLoadingBlogs = true;
    this.blogsService.getFeed(this.currentUser.id).subscribe({
      next: (blogs) => {
        this.blogs = this.sortBlogs(blogs);
        this.errorMessage = '';
        this.isLoadingBlogs = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesno ucitavanje blogova.';
        this.infoMessage = '';
        this.isLoadingBlogs = false;
      }
    });
  }

  loadFollowerData(): void {
    if (!this.currentUser) {
      return;
    }

    this.isLoadingFollowers = true;
    this.followersErrorMessage = '';

    this.followersService.getFollowing(this.currentUser.id).subscribe({
      next: (response) => {
        this.following = response.users;
        this.isLoadingFollowers = false;
      },
      error: (error: HttpErrorResponse) => {
        this.followersErrorMessage = error.error?.message ?? 'Neuspesno ucitavanje pracenih korisnika.';
        this.following = [];
        this.isLoadingFollowers = false;
      }
    });

    this.followersService.getRecommendations(this.currentUser.id).subscribe({
      next: (response) => {
        this.recommendations = response.users;
      },
      error: () => {
        this.recommendations = [];
      }
    });
  }

  followUser(user: UserAccount | FollowerUser): void {
    if (!this.currentUser || this.updatingFollowUserId === user.id) {
      return;
    }

    this.updatingFollowUserId = user.id;
    this.followersErrorMessage = '';

    this.followersService
      .follow(this.currentUser.id, user.id, this.currentUser.username, user.username)
      .subscribe({
        next: () => {
          this.updatingFollowUserId = null;
          this.loadFollowerData();
          this.loadBlogs();
        },
        error: (error: HttpErrorResponse) => {
          this.followersErrorMessage = error.error?.message ?? 'Neuspesno pracenje korisnika.';
          this.updatingFollowUserId = null;
        }
      });
  }

  unfollowUser(user: UserAccount | FollowerUser): void {
    if (!this.currentUser || this.updatingFollowUserId === user.id) {
      return;
    }

    this.updatingFollowUserId = user.id;
    this.followersErrorMessage = '';

    this.followersService.unfollow(this.currentUser.id, user.id).subscribe({
      next: () => {
        this.updatingFollowUserId = null;
        this.loadFollowerData();
        this.loadBlogs();
      },
      error: (error: HttpErrorResponse) => {
        this.followersErrorMessage = error.error?.message ?? 'Neuspesno uklanjanje pracenja.';
        this.updatingFollowUserId = null;
      }
    });
  }

  isFollowing(userId: number): boolean {
    return this.following.some((user) => user.id === userId);
  }

  getVisibleUsers(): UserAccount[] {
    if (!this.currentUser) {
      return [];
    }

    return this.users.filter((user) => user.id !== this.currentUser?.id && !user.isBlocked);
  }

  createBlog(): void {
    if (!this.currentUser || this.isSavingBlog) {
      return;
    }

    const payload = this.buildPayload();
    if (!payload.title || !payload.description) {
      this.errorMessage = 'Title i Description su obavezni.';
      this.infoMessage = '';
      return;
    }

    this.isSavingBlog = true;
    this.blogsService.createBlog(this.currentUser.id, payload).subscribe({
      next: (blog) => {
        this.blogs = this.sortBlogs([blog, ...this.blogs.filter((item) => item.id !== blog.id)]);
        this.titleInput = '';
        this.descriptionInput = '';
        this.selectedImages = [];
        this.errorMessage = '';
        this.infoMessage = 'Blog je uspesno kreiran.';
        this.isSavingBlog = false;
        this.loadBlogs();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesno kreiranje bloga.';
        this.infoMessage = '';
        this.isSavingBlog = false;
      }
    });
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString('sr-RS');
  }

  openBlog(blogId: number): void {
    this.router.navigate(['/blogs', blogId]);
  }

  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    if (!files || files.length === 0) {
      return;
    }

    const selectedFiles = Array.from(files);
    if (selectedFiles.some((file) => !file.type.startsWith('image/'))) {
      this.errorMessage = 'Moras izabrati sliku.';
      this.infoMessage = '';
      input.value = '';
      return;
    }

    this.errorMessage = '';
    Promise.all(selectedFiles.map((file) => this.readFileAsDataUrl(file)))
      .then((images) => {
        this.selectedImages = [...this.selectedImages, ...images];
      })
      .catch(() => {
        this.errorMessage = 'Neuspesno ucitavanje slike.';
        this.infoMessage = '';
      })
      .finally(() => {
        input.value = '';
      });
  }

  removeSelectedImage(index: number): void {
    if (index < 0 || index >= this.selectedImages.length) {
      return;
    }

    this.selectedImages = this.selectedImages.filter((_, currentIndex) => currentIndex !== index);
  }

  private buildPayload(): CreateBlogRequest {
    return {
      title: this.titleInput.trim(),
      description: this.descriptionInput.trim(),
      images: this.selectedImages
        .map((image) => image.trim())
        .filter((image) => image.length > 0)
    };
  }

  private readFileAsDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        if (typeof reader.result !== 'string') {
          reject(new Error('Neuspesno ucitavanje slike.'));
          return;
        }

        resolve(reader.result);
      };
      reader.onerror = () => reject(new Error('Neuspesno ucitavanje slike.'));
      reader.readAsDataURL(file);
    });
  }

  private sortBlogs(blogs: BlogPost[]): BlogPost[] {
    return [...blogs].sort((a, b) =>
      new Date(b.createdAtUtc).getTime() - new Date(a.createdAtUtc).getTime()
    );
  }

  private loadUsers(): void {
    this.usersService.getAll().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: () => {
        this.users = [];
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
