import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { BlogPost } from '../models/blog-post';
import { CreateBlogRequest } from '../models/create-blog-request';
import { BlogComment } from '../models/blog-comment';
import { CreateCommentRequest } from '../models/create-comment-request';

@Injectable({
  providedIn: 'root'
})
export class BlogsService {
  private readonly blogsUrl = `${environment.blogApiBaseUrl}/blogs`;

  constructor(private readonly http: HttpClient) {}

  getAllBlogs(): Observable<BlogPost[]> {
    return this.http.get<BlogPost[]>(this.blogsUrl);
  }

  getBlogById(blogId: number): Observable<BlogPost> {
    return this.http.get<BlogPost>(`${this.blogsUrl}/${blogId}`);
  }

  createBlog(userId: number, payload: CreateBlogRequest): Observable<BlogPost> {
    return this.http.post<BlogPost>(this.blogsUrl, payload, {
      headers: {
        'X-User-Id': String(userId)
      }
    });
  }

  getComments(blogId: number): Observable<BlogComment[]> {
    return this.http.get<BlogComment[]>(`${this.blogsUrl}/${blogId}/comments`);
  }

  getCommentById(blogId: number, commentId: number): Observable<BlogComment> {
    return this.http.get<BlogComment>(`${this.blogsUrl}/${blogId}/comments/${commentId}`);
  }

  createComment(blogId: number, userId: number, payload: CreateCommentRequest): Observable<BlogComment> {
    return this.http.post<BlogComment>(`${this.blogsUrl}/${blogId}/comments`, payload, {
      headers: {
        'X-User-Id': String(userId)
      }
    });
  }

  updateComment(blogId: number, commentId: number, userId: number, payload: CreateCommentRequest): Observable<BlogComment> {
    return this.http.put<BlogComment>(`${this.blogsUrl}/${blogId}/comments/${commentId}`, payload, {
      headers: {
        'X-User-Id': String(userId)
      }
    });
  }

  deleteComment(blogId: number, commentId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.blogsUrl}/${blogId}/comments/${commentId}`, {
      headers: {
        'X-User-Id': String(userId)
      }
    });
  }
}
