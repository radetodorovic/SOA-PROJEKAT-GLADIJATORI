import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthComponent } from './pages/auth/auth.component';
import { AdminUsersComponent } from './pages/admin-users/admin-users.component';
import { MyProfileComponent } from './pages/my-profile/my-profile.component';
import { HomeComponent } from './pages/home/home.component';
import { BlogsComponent } from './pages/blogs/blogs.component';
import { BlogDetailsComponent } from './pages/blog-details/blog-details.component';

const routes: Routes = [
  { path: '', redirectTo: 'auth', pathMatch: 'full' },
  { path: 'auth', component: AuthComponent },
  { path: 'home', component: HomeComponent },
  { path: 'blogs', component: BlogsComponent },
  { path: 'blogs/:id', component: BlogDetailsComponent },
  { path: 'admin/users', component: AdminUsersComponent },
  { path: 'my-profile', component: MyProfileComponent },
  { path: '**', redirectTo: 'auth' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
