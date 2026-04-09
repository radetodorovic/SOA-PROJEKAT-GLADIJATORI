import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { UsersService } from '../../services/users.service';
import { UserProfile } from '../../models/user-profile';

type AppRole = 'Admin' | 'Guide' | 'Tourist';

interface CurrentUser {
  id: number;
  username: string;
  email: string;
  role: AppRole;
  isBlocked: boolean;
}

interface ProfileDetails {
  firstName: string;
  lastName: string;
  profileImage: string;
  biography: string;
  motto: string;
}

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['./my-profile.component.css']
})
export class MyProfileComponent implements OnInit {
  currentUser: CurrentUser | null = null;
  profile: ProfileDetails = {
    firstName: '',
    lastName: '',
    profileImage: '',
    biography: '',
    motto: ''
  };
  isProfileInitialized = false;
  isLoadingProfile = true;
  isSavingProfile = false;
  isEditMode = false;

  editProfile: ProfileDetails = {
    firstName: '',
    lastName: '',
    profileImage: '',
    biography: '',
    motto: ''
  };

  firstNameInput = '';
  lastNameInput = '';
  biographyInput = '';
  mottoInput = '';

  showFirstNameForm = false;
  showLastNameForm = false;
  showBiographyForm = false;
  showMottoForm = false;

  errorMessage = '';
  infoMessage = '';

  constructor(
    private readonly router: Router,
    private readonly usersService: UsersService
  ) {}

  ngOnInit(): void {
    const user = this.readCurrentUser();
    if (!user) {
      this.router.navigate(['/auth']);
      return;
    }

    if (user.role === 'Admin') {
      this.router.navigate(['/admin/users']);
      return;
    }

    this.currentUser = user;
    this.loadProfile(user.id);
  }

  addFirstName(): void {
    if (!this.currentUser || this.profile.firstName || this.isProfileInitialized) {
      return;
    }

    const value = this.firstNameInput.trim();
    if (!value) {
      this.errorMessage = 'Ime je obavezno.';
      this.infoMessage = '';
      return;
    }

    this.profile.firstName = value;
    this.firstNameInput = '';
    this.showFirstNameForm = false;
    this.tryInitializeProfile('Ime je uneto. Profil ce biti sacuvan kada popunis sva polja.');
  }

  addLastName(): void {
    if (!this.currentUser || this.profile.lastName || this.isProfileInitialized) {
      return;
    }

    const value = this.lastNameInput.trim();
    if (!value) {
      this.errorMessage = 'Prezime je obavezno.';
      this.infoMessage = '';
      return;
    }

    this.profile.lastName = value;
    this.lastNameInput = '';
    this.showLastNameForm = false;
    this.tryInitializeProfile('Prezime je uneto. Profil ce biti sacuvan kada popunis sva polja.');
  }

  addBiography(): void {
    if (!this.currentUser || this.profile.biography || this.isProfileInitialized) {
      return;
    }

    const value = this.biographyInput.trim();
    if (!value) {
      this.errorMessage = 'Biografija je obavezna.';
      this.infoMessage = '';
      return;
    }

    this.profile.biography = value;
    this.biographyInput = '';
    this.showBiographyForm = false;
    this.tryInitializeProfile('Biografija je uneta. Profil ce biti sacuvan kada popunis sva polja.');
  }

  addMotto(): void {
    if (!this.currentUser || this.profile.motto || this.isProfileInitialized) {
      return;
    }

    const value = this.mottoInput.trim();
    if (!value) {
      this.errorMessage = 'Moto je obavezan.';
      this.infoMessage = '';
      return;
    }

    this.profile.motto = value;
    this.mottoInput = '';
    this.showMottoForm = false;
    this.tryInitializeProfile('Moto je unet. Profil ce biti sacuvan kada popunis sva polja.');
  }

  onImageSelected(event: Event): void {
    if (!this.currentUser || this.profile.profileImage || this.isProfileInitialized) {
      return;
    }

    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Moras izabrati sliku.';
      this.infoMessage = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result;
      if (typeof result !== 'string') {
        this.errorMessage = 'Neuspesno ucitavanje slike.';
        this.infoMessage = '';
        return;
      }

      this.profile.profileImage = result;
      this.tryInitializeProfile('Profilna slika je uneta. Profil ce biti sacuvan kada popunis sva polja.');
    };

    reader.readAsDataURL(file);
  }

  onEditImageSelected(event: Event): void {
    if (!this.isEditMode) {
      return;
    }

    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Moras izabrati sliku.';
      this.infoMessage = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result;
      if (typeof result !== 'string') {
        this.errorMessage = 'Neuspesno ucitavanje slike.';
        this.infoMessage = '';
        return;
      }

      this.editProfile.profileImage = result;
    };

    reader.readAsDataURL(file);
  }

  startEditing(): void {
    if (!this.isProfileInitialized || this.isLoadingProfile || this.isSavingProfile) {
      return;
    }

    this.editProfile = { ...this.profile };
    this.isEditMode = true;
    this.errorMessage = '';
    this.infoMessage = '';
  }

  cancelEditing(): void {
    if (!this.isEditMode || this.isSavingProfile) {
      return;
    }

    this.editProfile = { ...this.profile };
    this.isEditMode = false;
    this.errorMessage = '';
    this.infoMessage = '';
  }

  saveEditedProfile(): void {
    if (!this.currentUser || !this.isEditMode || this.isSavingProfile) {
      return;
    }

    const payload = this.normalizeProfile(this.editProfile);
    if (!this.isProfileCompleteFor(payload)) {
      this.errorMessage = 'Sva polja profila su obavezna.';
      this.infoMessage = '';
      return;
    }

    this.isSavingProfile = true;
    this.usersService.updateMyProfile(this.currentUser.id, payload).subscribe({
      next: (response) => {
        this.applyProfile(response);
        this.isEditMode = false;
        this.infoMessage = 'Profil je uspesno izmenjen.';
        this.errorMessage = '';
        this.isSavingProfile = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesna izmena profila.';
        this.infoMessage = '';
        this.isSavingProfile = false;
      }
    });
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.router.navigate(['/auth']);
  }

  getRoleLabel(role: AppRole): string {
    switch (role) {
      case 'Admin':
        return 'Administrator';
      case 'Guide':
        return 'Vodic';
      case 'Tourist':
        return 'Turista';
      default:
        return role;
    }
  }

  private loadProfile(userId: number): void {
    this.isLoadingProfile = true;
    this.usersService.getMyProfile(userId).subscribe({
      next: (profile) => {
        this.applyProfile(profile);
        this.errorMessage = '';
        this.infoMessage = '';
        this.isLoadingProfile = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesno ucitavanje profila.';
        this.infoMessage = '';
        this.isLoadingProfile = false;
      }
    });
  }

  private tryInitializeProfile(partialMessage: string): void {
    if (!this.currentUser || this.isSavingProfile) {
      return;
    }

    this.errorMessage = '';

    if (!this.isProfileComplete()) {
      this.infoMessage = partialMessage;
      return;
    }

    this.isSavingProfile = true;
    this.usersService.initializeMyProfile(this.currentUser.id, this.profile).subscribe({
      next: (response) => {
        this.applyProfile(response);
        this.infoMessage = 'Profil je uspesno sacuvan u bazi.';
        this.errorMessage = '';
        this.isSavingProfile = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'Neuspesno cuvanje profila.';
        this.infoMessage = '';
        this.isSavingProfile = false;

        if (error.status === 409) {
          this.loadProfile(this.currentUser!.id);
        }
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
      if (!parsed?.id || !parsed?.role) {
        return null;
      }

      return parsed;
    } catch {
      return null;
    }
  }

  private isProfileComplete(): boolean {
    return this.isProfileCompleteFor(this.profile);
  }

  private isProfileCompleteFor(profile: ProfileDetails): boolean {
    return !!profile.firstName &&
      !!profile.lastName &&
      !!profile.profileImage &&
      !!profile.biography &&
      !!profile.motto;
  }

  private normalizeProfile(profile: ProfileDetails): ProfileDetails {
    return {
      firstName: profile.firstName.trim(),
      lastName: profile.lastName.trim(),
      profileImage: profile.profileImage.trim(),
      biography: profile.biography.trim(),
      motto: profile.motto.trim()
    };
  }

  private applyProfile(profile: UserProfile): void {
    this.profile = {
      firstName: profile.firstName ?? '',
      lastName: profile.lastName ?? '',
      profileImage: profile.profileImage ?? '',
      biography: profile.biography ?? '',
      motto: profile.motto ?? ''
    };
    this.editProfile = { ...this.profile };
    this.isProfileInitialized = profile.isProfileInitialized;

    if (this.isProfileInitialized) {
      this.showFirstNameForm = false;
      this.showLastNameForm = false;
      this.showBiographyForm = false;
      this.showMottoForm = false;
    }
  }
}
