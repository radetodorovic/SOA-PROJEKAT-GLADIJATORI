import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegisterUserRequest } from '../../models/register-user-request';
import { LoginUserRequest } from '../../models/login-user-request';

type AuthMode = 'login' | 'register';
type RegisterRole = RegisterUserRequest['role'];

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  mode: AuthMode = 'login';
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  readonly loginForm = this.formBuilder.nonNullable.group({
    identifier: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  readonly registerForm = this.formBuilder.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      role: ['Tourist', [Validators.required]]
    },
    {
      validators: [AuthComponent.passwordsMatchValidator]
    }
  );

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  setMode(mode: AuthMode): void {
    this.mode = mode;
    this.successMessage = '';
    this.errorMessage = '';
  }

  submitLogin(): void {
    if (this.loginForm.invalid || this.isSubmitting) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const payload = this.loginForm.getRawValue() as LoginUserRequest;

    this.authService.login(payload).subscribe({
      next: (response) => {
        localStorage.setItem(
          'currentUser',
          JSON.stringify({
            id: response.id,
            username: response.username,
            email: response.email,
            role: response.role,
            isBlocked: response.isBlocked
          })
        );

        this.successMessage = `${response.message} Role: ${response.role}.`;
        this.isSubmitting = false;

        if (response.role === 'Admin') {
          this.router.navigate(['/admin/users']);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Login trenutno nije uspeo.';
        this.isSubmitting = false;
      }
    });
  }

  submitRegister(): void {
    if (this.registerForm.invalid || this.isSubmitting) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const rawValue = this.registerForm.getRawValue();
    const payload: RegisterUserRequest = {
      username: rawValue.username,
      email: rawValue.email,
      password: rawValue.password,
      role: rawValue.role as RegisterRole
    };

    this.authService.register(payload).subscribe({
      next: (user) => {
        this.successMessage = `Nalog za korisnika ${user.username} je uspesno kreiran kao ${user.role}.`;
        this.isSubmitting = false;
        this.registerForm.reset({
          username: '',
          email: '',
          password: '',
          confirmPassword: '',
          role: 'Tourist'
        });
        this.mode = 'login';
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Registracija trenutno nije uspela.';
        this.isSubmitting = false;
      }
    });
  }

  hasLoginError(controlName: 'identifier' | 'password'): boolean {
    const control = this.loginForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  hasRegisterError(controlName: 'username' | 'email' | 'password' | 'confirmPassword' | 'role'): boolean {
    const control = this.registerForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  hasPasswordMismatch(): boolean {
    return !!this.registerForm.errors?.['passwordMismatch'] &&
      (this.registerForm.controls.confirmPassword.dirty || this.registerForm.controls.confirmPassword.touched);
  }

  private static passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;

    if (!password || !confirmPassword) {
      return null;
    }

    return password === confirmPassword ? null : { passwordMismatch: true };
  }
}
