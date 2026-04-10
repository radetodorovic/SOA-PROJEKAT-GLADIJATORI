import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { BlogsService } from '../../services/blogs.service';
import { BlogPost } from '../../models/blog-post';
import { BlogComment } from '../../models/blog-comment';
import { UsersService } from '../../services/users.service';
import { UserAccount } from '../../models/user-account';

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
  currentUser: CurrentUser | null = null;
  blog: BlogPost | null = null;
  comments: BlogComment[] = [];
  likesCount = 0;
  isLikedByCurrentUser = false;

  isLoading = true;
  isLoadingComments = false;
  isSubmittingComment = false;
  isUpdatingLike = false;

  editingCommentId: number | null = null;
  editingCommentText = '';
  deletingCommentId: number | null = null;

  newCommentText = '';

  errorMessage = '';
  likesErrorMessage = '';
  commentsErrorMessage = '';
  commentsInfoMessage = '';

  private readonly authorLabels: Record<number, string> = {};
  private readonly loadingAuthorIds = new Set<number>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly blogsService: BlogsService,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    const user = this.readCurrentUser();
    if (!user) {
      this.router.navigate(['/auth']);
      return;
    }

    this.currentUser = user;

    this.route.paramMap.subscribe((params) => {
      const blogId = Number(params.get('id'));
      if (!Number.isInteger(blogId) || blogId <= 0) {
        this.errorMessage = 'Neispravan id bloga.';
        this.blog = null;
        this.comments = [];
        this.likesCount = 0;
        this.isLikedByCurrentUser = false;
        this.isLoading = false;
        this.isLoadingComments = false;
        return;
      }

      this.loadBlog(blogId);
      this.loadLikes(blogId);
      this.loadComments(blogId);
    });
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString('sr-RS');
  }

  getAuthorLabel(userId: number): string {
    return this.authorLabels[userId] ?? `Korisnik #${userId}`;
  }

  isEdited(comment: BlogComment): boolean {
    return comment.updatedAtUtc !== comment.createdAtUtc;
  }

  canManageComment(comment: BlogComment): boolean {
    return this.currentUser?.id === comment.userId;
  }

  toggleLike(): void {
    if (!this.currentUser || !this.blog || this.isUpdatingLike) {
      return;
    }

    this.isUpdatingLike = true;
    this.likesErrorMessage = '';

    const request$ = this.isLikedByCurrentUser
      ? this.blogsService.unlikeBlog(this.blog.id, this.currentUser.id)
      : this.blogsService.likeBlog(this.blog.id, this.currentUser.id);

    request$.subscribe({
      next: (response) => {
        this.likesCount = response.likesCount;
        this.isLikedByCurrentUser = !this.isLikedByCurrentUser;
        this.isUpdatingLike = false;
      },
      error: (error: HttpErrorResponse) => {
        this.likesErrorMessage = error.error?.message ?? 'Neuspesna promena lajka.';
        this.isUpdatingLike = false;
      }
    });
  }

  submitComment(): void {
    if (!this.currentUser || this.isSubmittingComment || !this.blog) {
      return;
    }

    const text = this.newCommentText.trim();
    if (!text) {
      this.commentsErrorMessage = 'Tekst komentara je obavezan.';
      this.commentsInfoMessage = '';
      return;
    }

    if (text.length > 5000) {
      this.commentsErrorMessage = 'Komentar moze imati maksimalno 5000 karaktera.';
      this.commentsInfoMessage = '';
      return;
    }

    this.isSubmittingComment = true;
    this.commentsErrorMessage = '';
    this.commentsInfoMessage = '';

    this.blogsService.createComment(this.blog.id, this.currentUser.id, { text }).subscribe({
      next: (comment) => {
        this.comments = [comment, ...this.comments.filter((item) => item.id !== comment.id)];
        this.newCommentText = '';
        this.commentsErrorMessage = '';
        this.commentsInfoMessage = 'Komentar je uspesno dodat.';
        this.isSubmittingComment = false;
        this.ensureAuthorLabel(comment.userId);
      },
      error: (error: HttpErrorResponse) => {
        this.commentsErrorMessage = error.error?.message ?? 'Neuspesno kreiranje komentara.';
        this.commentsInfoMessage = '';
        this.isSubmittingComment = false;
      }
    });
  }

  startEditComment(comment: BlogComment): void {
    if (!this.canManageComment(comment) || this.isSubmittingComment) {
      return;
    }

    this.editingCommentId = comment.id;
    this.editingCommentText = comment.text;
    this.commentsErrorMessage = '';
    this.commentsInfoMessage = '';
  }

  cancelEditComment(): void {
    this.editingCommentId = null;
    this.editingCommentText = '';
  }

  saveEditedComment(comment: BlogComment): void {
    if (!this.currentUser || !this.blog || this.editingCommentId !== comment.id || this.isSubmittingComment) {
      return;
    }

    const text = this.editingCommentText.trim();
    if (!text) {
      this.commentsErrorMessage = 'Tekst komentara je obavezan.';
      this.commentsInfoMessage = '';
      return;
    }

    if (text.length > 5000) {
      this.commentsErrorMessage = 'Komentar moze imati maksimalno 5000 karaktera.';
      this.commentsInfoMessage = '';
      return;
    }

    this.isSubmittingComment = true;
    this.commentsErrorMessage = '';
    this.commentsInfoMessage = '';

    this.blogsService.updateComment(this.blog.id, comment.id, this.currentUser.id, { text }).subscribe({
      next: (updatedComment) => {
        this.comments = this.comments.map((item) =>
          item.id === updatedComment.id ? updatedComment : item
        );
        this.cancelEditComment();
        this.commentsErrorMessage = '';
        this.commentsInfoMessage = 'Komentar je uspesno azuriran.';
        this.isSubmittingComment = false;
      },
      error: (error: HttpErrorResponse) => {
        this.commentsErrorMessage = error.error?.message ?? 'Neuspesno azuriranje komentara.';
        this.commentsInfoMessage = '';
        this.isSubmittingComment = false;
      }
    });
  }

  deleteComment(comment: BlogComment): void {
    if (!this.currentUser || !this.blog || !this.canManageComment(comment) || this.isSubmittingComment) {
      return;
    }

    const shouldDelete = window.confirm('Da li si siguran da zelis da obrises komentar?');
    if (!shouldDelete) {
      return;
    }

    this.isSubmittingComment = true;
    this.deletingCommentId = comment.id;
    this.commentsErrorMessage = '';
    this.commentsInfoMessage = '';

    this.blogsService.deleteComment(this.blog.id, comment.id, this.currentUser.id).subscribe({
      next: () => {
        this.comments = this.comments.filter((item) => item.id !== comment.id);
        if (this.editingCommentId === comment.id) {
          this.cancelEditComment();
        }

        this.commentsErrorMessage = '';
        this.commentsInfoMessage = 'Komentar je uspesno obrisan.';
        this.isSubmittingComment = false;
        this.deletingCommentId = null;
      },
      error: (error: HttpErrorResponse) => {
        this.commentsErrorMessage = error.error?.message ?? 'Neuspesno brisanje komentara.';
        this.commentsInfoMessage = '';
        this.isSubmittingComment = false;
        this.deletingCommentId = null;
      }
    });
  }

  private loadBlog(blogId: number): void {
    this.isLoading = true;
    this.blogsService.getBlogById(blogId).subscribe({
      next: (blog) => {
        this.blog = blog;
        this.errorMessage = '';
        this.isLoading = false;
        this.ensureAuthorLabel(blog.authorId);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesno ucitavanje bloga.';
        this.blog = null;
        this.isLoading = false;
      }
    });
  }

  private loadComments(blogId: number): void {
    this.isLoadingComments = true;
    this.commentsErrorMessage = '';
    this.commentsInfoMessage = '';
    this.editingCommentId = null;
    this.editingCommentText = '';

    this.blogsService.getComments(blogId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.isLoadingComments = false;

        const uniqueAuthorIds = Array.from(new Set(comments.map((comment) => comment.userId)));
        uniqueAuthorIds.forEach((authorId) => this.ensureAuthorLabel(authorId));
      },
      error: (error: HttpErrorResponse) => {
        this.commentsErrorMessage = error.error?.message ?? 'Neuspesno ucitavanje komentara.';
        this.comments = [];
        this.isLoadingComments = false;
      }
    });
  }

  private loadLikes(blogId: number): void {
    this.likesErrorMessage = '';
    this.likesCount = 0;
    this.isLikedByCurrentUser = false;

    this.blogsService.getLikesCount(blogId).subscribe({
      next: (count) => {
        this.likesCount = count;
      },
      error: (error: HttpErrorResponse) => {
        this.likesErrorMessage = error.error?.message ?? 'Neuspesno ucitavanje broja lajkova.';
      }
    });

    if (!this.currentUser) {
      return;
    }

    this.blogsService.isLikedByUser(blogId, this.currentUser.id).subscribe({
      next: (isLiked) => {
        this.isLikedByCurrentUser = isLiked;
      },
      error: () => {
        this.isLikedByCurrentUser = false;
      }
    });
  }

  private ensureAuthorLabel(userId: number): void {
    if (this.authorLabels[userId] || this.loadingAuthorIds.has(userId)) {
      return;
    }

    this.loadingAuthorIds.add(userId);
    this.usersService.getUserById(userId).subscribe({
      next: (user) => {
        this.authorLabels[userId] = this.buildAuthorLabel(user);
        this.loadingAuthorIds.delete(userId);
      },
      error: () => {
        this.authorLabels[userId] = `Korisnik #${userId}`;
        this.loadingAuthorIds.delete(userId);
      }
    });
  }

  private buildAuthorLabel(user: UserAccount): string {
    if (user.username?.trim()) {
      return `${user.username} (#${user.id})`;
    }

    return `Korisnik #${user.id}`;
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
