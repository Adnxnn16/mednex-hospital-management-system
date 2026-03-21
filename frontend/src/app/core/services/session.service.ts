import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private warningShown = false;
  private intervalId: any;

  constructor(private snackBar: MatSnackBar, private router: Router) {}

  startExpiryWatch(token: string): void {
    if (!token || token.split('.').length < 2) return;
    clearInterval(this.intervalId);
    this.warningShown = false;

    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    const expiresAt = (payload.exp ?? 0) * 1000;

    this.intervalId = setInterval(() => {
      const remaining = expiresAt - Date.now();

      if (remaining <= 0) {
        clearInterval(this.intervalId);
        this.logout();
        return;
      }

      if (remaining < 5 * 60 * 1000 && !this.warningShown) {
        this.warningShown = true;
        const minutes = Math.ceil(remaining / 60000);
        const ref = this.snackBar.open(
          `Your session expires in ${minutes} minute(s).`,
          'Refresh Session',
          { duration: 0, panelClass: ['session-warning-snackbar'] }
        );
        ref.onAction().subscribe(() => this.silentRefresh());
      }
    }, 30_000);
  }

  silentRefresh(): void {
    const iframe = document.createElement('iframe');
    iframe.style.display = 'none';
    iframe.src = '/silent-check-sso.html';
    document.body.appendChild(iframe);
    setTimeout(() => iframe.remove(), 5000);
    this.warningShown = false;
  }

  stopExpiryWatch(): void {
    clearInterval(this.intervalId);
  }

  logout(): void {
    this.stopExpiryWatch();
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
