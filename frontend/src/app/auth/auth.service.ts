import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { from, Observable, map } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

import { keycloakIssuer, oauthConfig } from './auth.config';

type JwtPayload = {
  realm_access?: { roles?: string[] };
  resource_access?: { [key: string]: { roles?: string[] } };
  tenant?: string;
  preferred_username?: string;
  email?: string;
  sub?: string;
  exp?: number;
};

function decodeJwt(token: string): JwtPayload {
  const parts = token.split('.');
  if (parts.length < 2) return {};
  const json = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
  return JSON.parse(decodeURIComponent(escape(json))) as JwtPayload;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _cachedToken: string | null = null;
  private _cachedClaims: JwtPayload | null = null;
  private _cachedRoles: string[] | null = null;
  private _mockRole: string | null = localStorage.getItem('mednex_mock_role');
  private _expiryTimer?: any;

  constructor(
    private readonly oauthService: OAuthService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {
    this.setupSessionMonitoring();
  }

  isLoggedIn(): boolean {
    return !!this._mockRole || this.oauthService.hasValidAccessToken();
  }

  accessToken(): string {
    return this.oauthService.getAccessToken();
  }

  refreshToken(): Observable<string> {
    return from(this.oauthService.refreshToken()).pipe(
      map(() => this.accessToken())
    );
  }

  private setupSessionMonitoring() {
    this.oauthService.events.subscribe(e => {
      if (e.type === 'token_received') {
        this.startExpiryTimer();
      }
    });
  }

  private startExpiryTimer() {
    if (this._expiryTimer) clearTimeout(this._expiryTimer);
    
    const token = this.accessToken();
    if (!token) return;

    const claims = decodeJwt(token);
    if (!claims.exp) return;

    const expiryTime = claims.exp * 1000;
    const now = Date.now();
    const warnAt = expiryTime - now - (5 * 60 * 1000); // 5 minutes warning

    if (warnAt > 0) {
      this._expiryTimer = setTimeout(() => {
        this.snackBar.open('Session will expire in 5 minutes. Please refresh your page.', 'Refresh', {
          duration: 10000,
        }).onAction().subscribe(() => this.refreshToken().subscribe());
      }, warnAt);
    }
  }

  claims(): JwtPayload {
    if (this._mockRole) {
      return { preferred_username: 'Dev User', realm_access: { roles: [this._mockRole] } };
    }
    const t = this.accessToken();
    if (!t) return {};
    if (t === this._cachedToken && this._cachedClaims) {
      return this._cachedClaims;
    }
    this._cachedToken = t;
    this._cachedClaims = decodeJwt(t);
    return this._cachedClaims;
  }

  roles(): string[] {
    if (this._mockRole) return [this._mockRole];

    const t = this.accessToken();
    if (t === this._cachedToken && this._cachedRoles) {
      return this._cachedRoles;
    }
    
    const claims = this.claims();
    const realmRoles = claims.realm_access?.roles ?? [];
    const clientRoles = claims.resource_access?.['mednex-frontend']?.roles ?? [];
    this._cachedRoles = [...new Set([...realmRoles, ...clientRoles])];
    return this._cachedRoles;
  }

  tenant(): string | null {
    return this.claims().tenant ?? null;
  }

  username(): string {
    const c = this.claims();
    return c.preferred_username ?? c.sub ?? 'user';
  }

  roleLabel(): string {
    const roles = this.roles();
    if (roles.includes('ROLE_ADMIN')) return 'Chief Administrator';
    if (roles.includes('ROLE_DOCTOR')) return 'Physician / Specialist';
    if (roles.includes('ROLE_NURSE')) return 'Clinical Staff';
    return 'User';
  }

  roleBaseRoute(): string {
    const roles = this.roles();
    if (roles.includes('ROLE_ADMIN')) return '/admin';
    if (roles.includes('ROLE_DOCTOR')) return '/doctor';
    if (roles.includes('ROLE_NURSE')) return '/nurse';
    return '';
  }

  roleSettingsRoute(): string {
    return this.roleBaseRoute() + '/settings';
  }

  setMockRole(role: 'ROLE_ADMIN' | 'ROLE_DOCTOR' | 'ROLE_NURSE' | null) {
    this._mockRole = role;
    if (role) {
      localStorage.setItem('mednex_mock_role', role);
    } else {
      localStorage.removeItem('mednex_mock_role');
    }
  }

  login(roleHint?: 'admin' | 'doctor' | 'nurse') {
    this.setMockRole(null); // Clear mock on real login attempt
    const params = roleHint ? { login_hint: roleHint } : undefined;
    this.oauthService.initLoginFlow(undefined, params);
  }

  signup(roleHint?: 'admin' | 'doctor' | 'nurse') {
    const redirectUri = oauthConfig.redirectUri as string;
    const url = new URL(`${keycloakIssuer}/protocol/openid-connect/registrations`);
    url.searchParams.set('client_id', 'mednex-frontend');
    url.searchParams.set('response_type', 'code');
    url.searchParams.set('scope', 'openid profile email');
    url.searchParams.set('redirect_uri', redirectUri);
    if (roleHint) url.searchParams.set('login_hint', roleHint);
    window.location.href = url.toString();
  }

  navigateToRoleDashboard(): void {
    const roles = this.roles();
    if (roles.includes('ROLE_ADMIN')) {
      this.router.navigateByUrl('/admin/dashboard');
    } else if (roles.includes('ROLE_DOCTOR')) {
      this.router.navigateByUrl('/doctor/dashboard');
    } else if (roles.includes('ROLE_NURSE')) {
      this.router.navigateByUrl('/nurse/dashboard');
    } else {
      this.router.navigateByUrl('/');
    }
  }

  async logout() {
    this.setMockRole(null);
    if (this._expiryTimer) clearTimeout(this._expiryTimer);
    this.oauthService.logOut();
    await this.router.navigateByUrl('/');
  }
}