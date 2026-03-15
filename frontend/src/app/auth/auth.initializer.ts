import { Router } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { oauthConfig } from './auth.config';

export function initializeAuth(oauth: OAuthService, router: Router) {
  return async () => {
    oauth.configure(oauthConfig);
    oauth.setStorage(localStorage);

    await oauth.loadDiscoveryDocumentAndTryLogin();

    // After a successful OIDC redirect (code exchange), navigate to role dashboard
    if (oauth.hasValidAccessToken()) {
      const token = oauth.getAccessToken();
      if (token) {
        const parts = token.split('.');
        if (parts.length >= 2) {
          const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
          const roles: string[] = payload?.realm_access?.roles ?? [];

          // Only redirect if we are currently on the OAuth callback path
          const path = window.location.pathname;
          if (path === '/auth/callback' || path === '/') {
            if (roles.includes('ROLE_ADMIN')) {
              await router.navigateByUrl('/admin/dashboard');
            } else if (roles.includes('ROLE_DOCTOR')) {
              await router.navigateByUrl('/doctor/dashboard');
            } else if (roles.includes('ROLE_NURSE')) {
              await router.navigateByUrl('/nurse/dashboard');
            }
          }
        }
      }
    }
  };
}