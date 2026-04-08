import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

type AppRole = 'Admin' | 'Guide' | 'Tourist';

interface CurrentUser {
  id: number;
  username: string;
  email: string;
  role: AppRole;
  isBlocked: boolean;
}

interface ProfileDetails {
  firstName: string | null;
  lastName: string | null;
  profileImage: string | null;
  biography: string | null;
  motto: string | null;
}

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['./my-profile.component.css']
})
export class MyProfileComponent implements OnInit {
  currentUser: CurrentUser | null = null;
  profile: ProfileDetails = {
    firstName: null,
    lastName: null,
    profileImage: null,
    biography: null,
    motto: null
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

  constructor(private readonly router: Router) {}

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
    this.profile = this.readProfile(user.id);
  }

  addFirstName(): void {
    if (!this.currentUser || this.profile.firstName) {
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
    this.saveAndNotify('Ime je uspesno sacuvano.');
  }

  addLastName(): void {
    if (!this.currentUser || this.profile.lastName) {
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
    this.saveAndNotify('Prezime je uspesno sacuvano.');
  }

  addBiography(): void {
    if (!this.currentUser || this.profile.biography) {
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
    this.saveAndNotify('Biografija je uspesno sacuvana.');
  }

  addMotto(): void {
    if (!this.currentUser || this.profile.motto) {
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
    this.saveAndNotify('Moto je uspesno sacuvan.');
  }

  onImageSelected(event: Event): void {
    if (!this.currentUser || this.profile.profileImage) {
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
      this.saveAndNotify('Profilna slika je uspesno sacuvana.');
    };

    reader.readAsDataURL(file);
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.router.navigate(['/auth']);
  }

  private saveAndNotify(message: string): void {
    if (!this.currentUser) {
      return;
    }

    localStorage.setItem(this.profileKey(this.currentUser.id), JSON.stringify(this.profile));
    this.errorMessage = '';
    this.infoMessage = message;
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

  private readProfile(userId: number): ProfileDetails {
    const raw = localStorage.getItem(this.profileKey(userId));
    if (!raw) {
      return {
        firstName: null,
        lastName: null,
        profileImage: null,
        biography: null,
        motto: null
      };
    }

    try {
      const parsed = JSON.parse(raw) as ProfileDetails;
      return {
        firstName: parsed.firstName ?? null,
        lastName: parsed.lastName ?? null,
        profileImage: parsed.profileImage ?? null,
        biography: parsed.biography ?? null,
        motto: parsed.motto ?? null
      };
    } catch {
      return {
        firstName: null,
        lastName: null,
        profileImage: null,
        biography: null,
        motto: null
      };
    }
  }

  private profileKey(userId: number): string {
    return `profile-details:${userId}`;
  }
}
