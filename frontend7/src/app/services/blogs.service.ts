import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { BlogPost } from '../models/blog-post';
import { CreateBlogRequest } from '../models/create-blog-request';

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
}
