import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import * as L from 'leaflet';
import { ToursService } from '../../services/tours.service';
import { Tour, KeyPoint, Review, TouristPosition, ShoppingCart, TourPurchaseToken, TourExecution } from '../../models/tour';

type AppRole = 'Admin' | 'Guide' | 'Tourist';

interface CurrentUser {
  id: number;
  username: string;
  email: string;
  role: AppRole;
  isBlocked: boolean;
}

@Component({
  selector: 'app-tours',
  templateUrl: './tours.component.html',
  styleUrls: ['./tours.component.css']
})
export class ToursComponent implements OnInit, OnDestroy {
  currentUser: CurrentUser | null = null;

  readonly difficulties = [
    { value: 'EASY',   label: 'Laka' },
    { value: 'MEDIUM', label: 'Srednja' },
    { value: 'HARD',   label: 'Teska' }
  ];

  readonly transportTypes = [
    { value: 'WALKING', label: 'Peske' },
    { value: 'BICYCLE', label: 'Bicikl' },
    { value: 'CAR', label: 'Automobil' }
  ];

  // ── Tour creation ───────────────────────────────────────────────────────────
  newTourName = '';
  newTourDesc = '';
  newTourPrice = 0;
  newTourDifficulty = 'EASY';
  newTourTagInput = '';
  newTourTags: string[] = [];
  isCreatingTour = false;

  // ── Tour lists ──────────────────────────────────────────────────────────────
  myTours: Tour[] = [];
  publishedTours: Tour[] = [];
  isLoadingTours = false;
  cart: ShoppingCart | null = null;
  purchaseTokens: TourPurchaseToken[] = [];
  isCartBusy = false;
  activeExecution: TourExecution | null = null;
  isExecutionBusy = false;
  private executionTimer: ReturnType<typeof setInterval> | null = null;

  // ── Selected tour ───────────────────────────────────────────────────────────
  selectedTour: Tour | null = null;
  editTourPrice = 0;
  editTourDifficulty = 'EASY';
  editTourTagInput = '';
  editTourTags: string[] = [];
  editWalkingDuration: number | null = null;
  editBicycleDuration: number | null = null;
  editCarDuration: number | null = null;
  isUpdatingTour = false;

  // ── Key point form ──────────────────────────────────────────────────────────
  kpName = '';
  kpDesc = '';
  kpLat: number | null = null;
  kpLng: number | null = null;
  kpOrder = 0;
  kpImageUrl = '';
  kpImagePreview: string | null = null;
  editingKeyPointId: string | null = null;
  isSavingKp = false;

  // ── Reviews ─────────────────────────────────────────────────────────────────
  reviews: Review[] = [];
  reviewRating = 5;
  reviewComment = '';
  reviewVisitDate = '';
  reviewImageUrls: string[] = [];
  isAddingReview = false;

  // ── Position simulator ──────────────────────────────────────────────────────
  posLat: number | null = null;
  posLng: number | null = null;
  currentPosition: TouristPosition | null = null;
  isUpdatingPosition = false;

  // ── Map ─────────────────────────────────────────────────────────────────────
  private leafletMap: L.Map | null = null;
  private touristMarker: L.Marker | null = null;
  private clickPreviewMarker: L.Marker | null = null;

  // ── Messages ────────────────────────────────────────────────────────────────
  errorMessage = '';
  successMessage = '';

  constructor(
    private readonly router: Router,
    private readonly toursService: ToursService
  ) {}

  ngOnInit(): void {
    const user = this.readCurrentUser();
    if (!user) {
      this.router.navigate(['/auth']);
      return;
    }
    this.currentUser = user;

    if (this.isGuide()) {
      this.loadMyTours();
    } else {
      this.loadPublishedTours();
      this.loadCart();
      this.loadPurchaseTokens();
    }

    this.loadMyPosition();
  }

  ngOnDestroy(): void {
    this.stopExecutionTimer();
    this.destroyMap();
  }

  isGuide(): boolean {
    return this.currentUser?.role === 'Guide';
  }

  isAuthorOfSelected(): boolean {
    return !!this.selectedTour && !!this.currentUser && this.selectedTour.authorId === this.currentUser.id;
  }

  getDifficultyLabel(value: string): string {
    return this.difficulties.find(d => d.value === value)?.label ?? value;
  }

  // ── Tours ───────────────────────────────────────────────────────────────────

  loadMyTours(): void {
    if (!this.currentUser) return;
    this.isLoadingTours = true;
    this.toursService.getToursByAuthor(this.currentUser.id).subscribe({
      next: tours => { this.myTours = tours; this.isLoadingTours = false; },
      error: () => { this.errorMessage = 'Neuspesno ucitavanje tura.'; this.isLoadingTours = false; }
    });
  }

  loadPublishedTours(): void {
    this.isLoadingTours = true;
    this.toursService.getPublishedTours().subscribe({
      next: tours => { this.publishedTours = tours; this.isLoadingTours = false; },
      error: () => { this.errorMessage = 'Neuspjesno ucitavanje tura.'; this.isLoadingTours = false; }
    });
  }

  selectTour(tour: Tour): void {
    if (!this.currentUser) return;
    this.toursService.getTourById(tour.id, this.currentUser.id).subscribe({
      next: fullTour => this.applySelectedTour(fullTour),
      error: () => this.applySelectedTour(tour)
    });
  }

  createTour(): void {
    if (!this.currentUser || this.isCreatingTour) return;
    if (!this.newTourName.trim() || !this.newTourDesc.trim()) {
      this.errorMessage = 'Naziv i opis ture su obavezni.';
      return;
    }
    if (this.newTourPrice < 0) { this.errorMessage = 'Cijena ne moze biti negativna.'; return; }
    this.isCreatingTour = true;
    this.toursService.createTour(
      this.currentUser.id,
      this.newTourName.trim(),
      this.newTourDesc.trim(),
      this.newTourPrice,
      this.newTourDifficulty,
      [...this.newTourTags]
    ).subscribe({
      next: tour => {
        this.myTours = [tour, ...this.myTours];
        this.newTourName = '';
        this.newTourDesc = '';
        this.newTourPrice = 0;
        this.newTourDifficulty = 'EASY';
        this.newTourTags = [];
        this.newTourTagInput = '';
        this.successMessage = 'Tura je uspesno kreirana.';
        this.errorMessage = '';
        this.isCreatingTour = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Neuspesno kreiranje ture.';
        this.isCreatingTour = false;
      }
    });
  }

  updateTourMeta(): void {
    if (!this.selectedTour || !this.currentUser || this.isUpdatingTour) return;
    if (this.editTourPrice < 0) { this.errorMessage = 'Cijena ne moze biti negativna.'; return; }
    this.isUpdatingTour = true;
    this.toursService.updateTour(this.selectedTour.id, this.currentUser.id, {
      price: this.editTourPrice,
      difficulty: this.editTourDifficulty,
      transportDurations: this.buildDurationPayload(),
      tags: [...this.editTourTags]
    }).subscribe({
      next: updated => {
        this.selectedTour = updated;
        this.myTours = this.myTours.map(t => t.id === updated.id ? updated : t);
        this.successMessage = 'Tura je azurirana.';
        this.isUpdatingTour = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri azuriranju ture.';
        this.isUpdatingTour = false;
      }
    });
  }

  publishTour(): void {
    if (!this.selectedTour || !this.currentUser || this.isUpdatingTour) return;
    this.isUpdatingTour = true;
    this.toursService.updateTour(this.selectedTour.id, this.currentUser.id, {
      price: this.editTourPrice,
      difficulty: this.editTourDifficulty,
      transportDurations: this.buildDurationPayload(),
      tags: [...this.editTourTags],
      status: 'PUBLISHED'
    }).subscribe({
      next: updated => {
        this.selectedTour = updated;
        this.myTours = this.myTours.map(t => t.id === updated.id ? updated : t);
        this.successMessage = 'Tura je objavljena.';
        this.isUpdatingTour = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri objavljivanju ture.';
        this.isUpdatingTour = false;
      }
    });
  }

  private applySelectedTour(tour: Tour): void {
    this.selectedTour = tour;
    this.editTourPrice = tour.price;
    this.editTourDifficulty = tour.difficulty ?? 'EASY';
    this.editTourTags = [...(tour.tags ?? [])];
    this.editWalkingDuration = tour.transportDurations?.['WALKING'] ?? null;
    this.editBicycleDuration = tour.transportDurations?.['BICYCLE'] ?? null;
    this.editCarDuration = tour.transportDurations?.['CAR'] ?? null;
    this.editTourTagInput = '';
    this.cancelEditKeyPoint();
    this.loadReviews(tour.id);
    this.activeExecution = null;
    this.stopExecutionTimer();
    if (!this.isGuide()) {
      this.loadActiveExecution(tour.id);
    }
    this.clearMessages();
    this.destroyMap();
    setTimeout(() => this.initMap(), 0);
  }

  archiveTour(): void {
    this.changeTourStatus('ARCHIVED', 'Tura je arhivirana.', 'Greska pri arhiviranju ture.');
  }

  // ── Shopping cart ───────────────────────────────────────────────────────────

  loadCart(): void {
    if (!this.currentUser || this.isGuide()) return;
    this.toursService.getCart(this.currentUser.id).subscribe({
      next: cart => this.cart = cart,
      error: () => this.cart = null
    });
  }

  loadPurchaseTokens(): void {
    if (!this.currentUser || this.isGuide()) return;
    this.toursService.getPurchaseTokens(this.currentUser.id).subscribe({
      next: tokens => this.purchaseTokens = tokens,
      error: () => this.purchaseTokens = []
    });
  }

  addSelectedToCart(): void {
    if (!this.currentUser || !this.selectedTour || this.isCartBusy) return;
    this.isCartBusy = true;
    this.toursService.addToCart(this.currentUser.id, this.selectedTour.id).subscribe({
      next: cart => {
        this.cart = cart;
        this.successMessage = 'Tura je dodata u korpu.';
        this.errorMessage = '';
        this.isCartBusy = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri dodavanju u korpu.';
        this.isCartBusy = false;
      }
    });
  }

  removeCartItem(tourId: string): void {
    if (!this.currentUser || this.isCartBusy) return;
    this.isCartBusy = true;
    this.toursService.removeFromCart(this.currentUser.id, tourId).subscribe({
      next: cart => {
        this.cart = cart;
        this.successMessage = 'Stavka je uklonjena iz korpe.';
        this.errorMessage = '';
        this.isCartBusy = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri uklanjanju iz korpe.';
        this.isCartBusy = false;
      }
    });
  }

  checkout(): void {
    if (!this.currentUser || this.isCartBusy) return;
    this.isCartBusy = true;
    this.toursService.checkout(this.currentUser.id).subscribe({
      next: tokens => {
        this.purchaseTokens = [...tokens, ...this.purchaseTokens.filter(existing => !tokens.some(t => t.tourId === existing.tourId))];
        this.loadCart();
        this.loadPublishedTours();
        if (this.selectedTour && tokens.some(t => t.tourId === this.selectedTour!.id)) {
          this.selectTour(this.selectedTour);
        }
        this.successMessage = 'Kupovina je uspesno zavrsena.';
        this.errorMessage = '';
        this.isCartBusy = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri checkout-u.';
        this.isCartBusy = false;
      }
    });
  }

  isSelectedPurchased(): boolean {
    return !!this.selectedTour && this.purchaseTokens.some(token => token.tourId === this.selectedTour!.id);
  }

  isSelectedInCart(): boolean {
    return !!this.selectedTour && !!this.cart?.items.some(item => item.tourId === this.selectedTour!.id);
  }

  // ── Tour execution ──────────────────────────────────────────────────────────

  loadActiveExecution(tourId: string): void {
    if (!this.currentUser || this.isGuide()) return;
    this.toursService.getActiveExecution(this.currentUser.id, tourId).subscribe({
      next: execution => {
        this.activeExecution = execution;
        this.startExecutionTimer();
      },
      error: () => {
        this.activeExecution = null;
        this.stopExecutionTimer();
      }
    });
  }

  startExecution(): void {
    if (!this.currentUser || !this.selectedTour || this.isExecutionBusy) return;
    this.isExecutionBusy = true;
    this.toursService.startExecution(this.currentUser.id, this.selectedTour.id).subscribe({
      next: execution => {
        this.activeExecution = execution;
        this.successMessage = 'Tura je pokrenuta.';
        this.errorMessage = '';
        this.isExecutionBusy = false;
        this.startExecutionTimer();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri pokretanju ture.';
        this.isExecutionBusy = false;
      }
    });
  }

  checkExecutionProgress(): void {
    if (!this.currentUser || !this.selectedTour || !this.activeExecution || this.isExecutionBusy) return;
    this.isExecutionBusy = true;
    this.toursService.checkExecution(this.currentUser.id, this.selectedTour.id).subscribe({
      next: execution => {
        const previousCount = this.activeExecution?.completedKeyPoints.length ?? 0;
        this.activeExecution = execution;
        if (execution.completedKeyPoints.length > previousCount) {
          this.successMessage = 'Kljucna tacka je dostignuta.';
          this.errorMessage = '';
        }
        this.isExecutionBusy = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri proveri pozicije.';
        this.isExecutionBusy = false;
      }
    });
  }

  completeExecution(): void {
    if (!this.currentUser || !this.selectedTour || !this.activeExecution || this.isExecutionBusy) return;
    this.isExecutionBusy = true;
    this.toursService.completeExecution(this.currentUser.id, this.selectedTour.id).subscribe({
      next: execution => {
        this.activeExecution = execution;
        this.successMessage = 'Tura je zavrsena.';
        this.errorMessage = '';
        this.isExecutionBusy = false;
        this.stopExecutionTimer();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri zavrsetku ture.';
        this.isExecutionBusy = false;
      }
    });
  }

  abandonExecution(): void {
    if (!this.currentUser || !this.selectedTour || !this.activeExecution || this.isExecutionBusy) return;
    this.isExecutionBusy = true;
    this.toursService.abandonExecution(this.currentUser.id, this.selectedTour.id).subscribe({
      next: execution => {
        this.activeExecution = execution;
        this.successMessage = 'Tura je napustena.';
        this.errorMessage = '';
        this.isExecutionBusy = false;
        this.stopExecutionTimer();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri napustanju ture.';
        this.isExecutionBusy = false;
      }
    });
  }

  canCompleteExecution(): boolean {
    return !!this.selectedTour
      && !!this.activeExecution
      && this.activeExecution.completedKeyPoints.length >= this.selectedTour.keyPoints.length;
  }

  private startExecutionTimer(): void {
    this.stopExecutionTimer();
    this.executionTimer = setInterval(() => this.checkExecutionProgress(), 10000);
  }

  private stopExecutionTimer(): void {
    if (this.executionTimer) {
      clearInterval(this.executionTimer);
      this.executionTimer = null;
    }
  }

  reactivateTour(): void {
    this.changeTourStatus('PUBLISHED', 'Tura je ponovo aktivirana.', 'Greska pri reaktivaciji ture.');
  }

  private changeTourStatus(status: string, success: string, failure: string): void {
    if (!this.selectedTour || !this.currentUser || this.isUpdatingTour) return;
    this.isUpdatingTour = true;
    this.toursService.updateTour(this.selectedTour.id, this.currentUser.id, { status }).subscribe({
      next: updated => {
        this.selectedTour = updated;
        this.myTours = this.myTours.map(t => t.id === updated.id ? updated : t);
        this.successMessage = success;
        this.errorMessage = '';
        this.isUpdatingTour = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? failure;
        this.isUpdatingTour = false;
      }
    });
  }

  // ── Key points ───────────────────────────────────────────────────────────────

  startEditKeyPoint(kp: KeyPoint): void {
    this.editingKeyPointId = kp.id;
    this.kpName = kp.name;
    this.kpDesc = kp.description ?? '';
    this.kpLat = kp.latitude;
    this.kpLng = kp.longitude;
    this.kpOrder = kp.order;
    this.kpImageUrl = kp.imageUrl ?? '';
  }

  cancelEditKeyPoint(): void {
    this.editingKeyPointId = null;
    this.kpName = '';
    this.kpDesc = '';
    this.kpLat = null;
    this.kpLng = null;
    this.kpOrder = 0;
    this.kpImageUrl = '';
    this.kpImagePreview = null;
    this.removeClickPreview();
  }

  saveKeyPoint(): void {
    if (!this.selectedTour || !this.currentUser || this.isSavingKp) return;
    if (!this.kpName.trim() || this.kpLat === null || this.kpLng === null) {
      this.errorMessage = 'Naziv, geografska sirina i duzina su obavezni.';
      return;
    }
    const payload = {
      name: this.kpName.trim(),
      description: this.kpDesc.trim() || null,
      latitude: this.kpLat,
      longitude: this.kpLng,
      order: this.kpOrder,
      imageUrl: this.kpImageUrl.trim() || null
    };
    this.isSavingKp = true;
    const request$ = this.editingKeyPointId
      ? this.toursService.updateKeyPoint(this.selectedTour.id, this.editingKeyPointId, this.currentUser.id, payload)
      : this.toursService.addKeyPoint(this.selectedTour.id, this.currentUser.id, payload);

    request$.subscribe({
      next: updatedTour => {
        this.selectedTour = updatedTour;
        this.myTours = this.myTours.map(t => t.id === updatedTour.id ? updatedTour : t);
        this.successMessage = this.editingKeyPointId ? 'Kljucna tacka je izmenjena.' : 'Kljucna tacka je dodata.';
        this.errorMessage = '';
        this.cancelEditKeyPoint();
        this.isSavingKp = false;
        this.destroyMap();
        setTimeout(() => this.initMap(), 0);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri cuvanju kljucne tacke.';
        this.isSavingKp = false;
      }
    });
  }

  deleteKeyPoint(kpId: string): void {
    if (!this.selectedTour || !this.currentUser) return;
    this.toursService.deleteKeyPoint(this.selectedTour.id, kpId, this.currentUser.id).subscribe({
      next: updatedTour => {
        this.selectedTour = updatedTour;
        this.myTours = this.myTours.map(t => t.id === updatedTour.id ? updatedTour : t);
        this.successMessage = 'Kljucna tacka je obrisana.';
        this.errorMessage = '';
        this.destroyMap();
        setTimeout(() => this.initMap(), 0);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri brisanju kljucne tacke.';
      }
    });
  }

  // ── Reviews ──────────────────────────────────────────────────────────────────

  loadReviews(tourId: string): void {
    this.toursService.getReviews(tourId).subscribe({
      next: reviews => this.reviews = reviews,
      error: () => this.reviews = []
    });
  }

  onKpImageSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      this.kpImageUrl = reader.result as string;
      this.kpImagePreview = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  onReviewImagesSelected(event: Event): void {
    const files = Array.from((event.target as HTMLInputElement).files ?? []);
    files.forEach(file => {
      const reader = new FileReader();
      reader.onload = () => {
        this.reviewImageUrls.push(reader.result as string);
      };
      reader.readAsDataURL(file);
    });
  }

  removeReviewImage(index: number): void {
    this.reviewImageUrls.splice(index, 1);
  }

  trackByIndex(index: number): number {
    return index;
  }

  addReview(): void {
    if (!this.selectedTour || !this.currentUser || this.isAddingReview) return;
    if (this.reviewRating < 1 || this.reviewRating > 5) {
      this.errorMessage = 'Ocena mora biti izmedju 1 i 5.';
      return;
    }
    const imageUrls = [...this.reviewImageUrls];
    this.isAddingReview = true;
    this.toursService.addReview(
      this.selectedTour.id,
      this.currentUser.id,
      this.currentUser.username,
      this.reviewRating,
      this.reviewComment.trim(),
      this.reviewVisitDate || null,
      imageUrls
    ).subscribe({
      next: review => {
        this.reviews = [review, ...this.reviews];
        this.reviewComment = '';
        this.reviewRating = 5;
        this.reviewVisitDate = '';
        this.reviewImageUrls = [];
        this.successMessage = 'Recenzija je objavljena.';
        this.errorMessage = '';
        this.isAddingReview = false;
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Neuspesno objavljivanje recenzije.';
        this.isAddingReview = false;
      }
    });
  }

  // ── Position simulator ───────────────────────────────────────────────────────

  updatePosition(): void {
    if (!this.currentUser || this.isUpdatingPosition) return;
    if (this.posLat === null || this.posLng === null) {
      this.errorMessage = 'Unesi geografsku sirinu i duzinu.';
      return;
    }
    this.isUpdatingPosition = true;
    this.toursService.updatePosition(this.currentUser.id, this.posLat, this.posLng).subscribe({
      next: () => {
        this.successMessage = 'Pozicija je azurirana.';
        this.errorMessage = '';
        this.isUpdatingPosition = false;
        const lat = this.posLat!;
        const lng = this.posLng!;
        this.currentPosition = {
          id: '',
          userId: this.currentUser!.id,
          latitude: lat,
          longitude: lng,
          updatedAt: new Date().toISOString()
        };
        this.addTouristMarker(lat, lng);
        this.loadMyPosition();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message ?? 'Greska pri azuriranju pozicije.';
        this.isUpdatingPosition = false;
      }
    });
  }

  loadMyPosition(): void {
    if (!this.currentUser) return;
    this.toursService.getPosition(this.currentUser.id).subscribe({
      next: pos => {
        this.currentPosition = pos;
        this.addTouristMarker(pos.latitude, pos.longitude);
      },
      error: () => this.currentPosition = null
    });
  }

  // ── Map ──────────────────────────────────────────────────────────────────────

  private initMap(): void {
    if (!this.selectedTour) return;
    const container = document.getElementById('tour-map');
    if (!container) return;

    this.leafletMap = L.map('tour-map', { zoomControl: true });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19
    }).addTo(this.leafletMap);

    const kps = [...this.selectedTour.keyPoints].sort((a, b) => a.order - b.order);

    if (kps.length === 0) {
      this.leafletMap.setView([44.0165, 21.0059], 7);
    } else {
      const coords: L.LatLngTuple[] = kps.map(kp => [kp.latitude, kp.longitude]);

      kps.forEach((kp, i) => {
        L.marker([kp.latitude, kp.longitude], { icon: this.getKpIcon(i + 1) })
          .addTo(this.leafletMap!)
          .bindPopup(`<strong>${kp.name}</strong>${kp.description ? '<br>' + kp.description : ''}`);
      });

      if (coords.length > 1) {
        L.polyline(coords, { color: '#7d5526', weight: 4, opacity: 0.85 }).addTo(this.leafletMap);
      }

      this.leafletMap.fitBounds(L.latLngBounds(coords), { padding: [40, 40] });
    }

    if (this.currentPosition) {
      this.addTouristMarker(this.currentPosition.latitude, this.currentPosition.longitude);
    }

    // Map click: Guide fills keypoint coords, Tourist fills position coords
    this.leafletMap.on('click', (e: L.LeafletMouseEvent) => {
      const lat = Math.round(e.latlng.lat * 1000000) / 1000000;
      const lng = Math.round(e.latlng.lng * 1000000) / 1000000;

      if (this.isGuide()) {
        this.kpLat = lat;
        this.kpLng = lng;
        this.successMessage = `Koordinate preuzete s mape: ${lat}, ${lng}`;
        this.showClickPreview(lat, lng);
      } else {
        this.posLat = lat;
        this.posLng = lng;
        this.successMessage = `Pozicija odabrana: ${lat}, ${lng} — klikni "Azuriraj poziciju" da sacuvas.`;
        this.addTouristMarker(lat, lng);
      }
    });
  }

  private showClickPreview(lat: number, lng: number): void {
    if (!this.leafletMap) return;
    if (this.clickPreviewMarker) {
      this.clickPreviewMarker.setLatLng([lat, lng]);
      return;
    }
    this.clickPreviewMarker = L.marker([lat, lng], { icon: this.getPreviewIcon() })
      .addTo(this.leafletMap)
      .bindPopup('Nova kljucna tacka (nije sacuvana)');
  }

  private removeClickPreview(): void {
    if (this.clickPreviewMarker) {
      this.clickPreviewMarker.remove();
      this.clickPreviewMarker = null;
    }
  }

  private addTouristMarker(lat: number, lng: number): void {
    if (!this.leafletMap) return;
    if (this.touristMarker) {
      this.touristMarker.setLatLng([lat, lng]);
      return;
    }
    this.touristMarker = L.marker([lat, lng], { icon: this.getTouristIcon() })
      .addTo(this.leafletMap)
      .bindPopup('<strong>Tvoja pozicija</strong>');
  }

  private destroyMap(): void {
    if (this.leafletMap) {
      this.leafletMap.remove();
      this.leafletMap = null;
      this.touristMarker = null;
      this.clickPreviewMarker = null;
    }
  }

  private getKpIcon(num: number): L.DivIcon {
    return L.divIcon({
      html: `<div style="width:30px;height:30px;border-radius:50%;background:#7d5526;color:#fff;font-size:13px;font-weight:700;display:flex;align-items:center;justify-content:center;border:2px solid #5a3a1a;box-shadow:0 2px 6px rgba(0,0,0,0.35);">${num}</div>`,
      className: '',
      iconSize: [30, 30],
      iconAnchor: [15, 15],
      popupAnchor: [0, -16]
    });
  }

  private getPreviewIcon(): L.DivIcon {
    return L.divIcon({
      html: `<div style="width:30px;height:30px;border-radius:50%;background:#f39c12;color:#fff;font-size:16px;display:flex;align-items:center;justify-content:center;border:2px solid #d68910;box-shadow:0 2px 6px rgba(0,0,0,0.35);">+</div>`,
      className: '',
      iconSize: [30, 30],
      iconAnchor: [15, 15],
      popupAnchor: [0, -16]
    });
  }

  private getTouristIcon(): L.DivIcon {
    return L.divIcon({
      html: `<div style="width:32px;height:32px;border-radius:50%;background:#e74c3c;color:#fff;font-size:18px;display:flex;align-items:center;justify-content:center;border:2px solid #c0392b;box-shadow:0 2px 6px rgba(0,0,0,0.35);">★</div>`,
      className: '',
      iconSize: [32, 32],
      iconAnchor: [16, 16],
      popupAnchor: [0, -17]
    });
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  getStars(rating: number): string {
    return '★'.repeat(rating) + '☆'.repeat(5 - rating);
  }

  formatDate(value: string | null): string {
    if (!value) return '';
    const date = new Date(value);
    return isNaN(date.getTime()) ? value : date.toLocaleString('sr-RS');
  }

  formatShortDate(value: string | null): string {
    if (!value) return '';
    const date = new Date(value);
    return isNaN(date.getTime()) ? value : date.toLocaleDateString('sr-RS');
  }

  addNewTourTag(): void {
    const tag = this.newTourTagInput.trim().toLowerCase();
    if (!tag || this.newTourTags.includes(tag)) { this.newTourTagInput = ''; return; }
    this.newTourTags.push(tag);
    this.newTourTagInput = '';
  }

  removeNewTourTag(tag: string): void {
    this.newTourTags = this.newTourTags.filter(t => t !== tag);
  }

  addEditTag(): void {
    const tag = this.editTourTagInput.trim().toLowerCase();
    if (!tag || this.editTourTags.includes(tag)) { this.editTourTagInput = ''; return; }
    this.editTourTags.push(tag);
    this.editTourTagInput = '';
  }

  removeEditTag(tag: string): void {
    this.editTourTags = this.editTourTags.filter(t => t !== tag);
  }

  getTransportLabel(type: string): string {
    return this.transportTypes.find(t => t.value === type)?.label ?? type;
  }

  getTransportEntries(tour: Tour): Array<{ type: string; minutes: number }> {
    return Object.entries(tour.transportDurations ?? {}).map(([type, minutes]) => ({ type, minutes }));
  }

  private buildDurationPayload(): Record<string, number> {
    const durations: Record<string, number> = {};
    if (this.editWalkingDuration && this.editWalkingDuration > 0) durations['WALKING'] = this.editWalkingDuration;
    if (this.editBicycleDuration && this.editBicycleDuration > 0) durations['BICYCLE'] = this.editBicycleDuration;
    if (this.editCarDuration && this.editCarDuration > 0) durations['CAR'] = this.editCarDuration;
    return durations;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private readCurrentUser(): CurrentUser | null {
    const raw = localStorage.getItem('currentUser');
    if (!raw) return null;
    try {
      const parsed = JSON.parse(raw) as CurrentUser;
      if (typeof parsed.id !== 'number' || !parsed.role) return null;
      return parsed;
    } catch {
      return null;
    }
  }
}
