import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';

import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const required = (route.data['roles'] as string[] | undefined) ?? [];
    if (required.length === 0) return true;

    const token = this.auth.getToken();
    let userRoles: string[] = [];
    if (token && token.split('.').length >= 2) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
        userRoles = payload?.realm_access?.roles ?? [];
      } catch {
        userRoles = this.auth.roles();
      }
    } else {
      userRoles = this.auth.roles();
    }

    const roles = new Set(userRoles);
    const ok = required.some((r) => roles.has(r));
    return ok ? true : this.router.createUrlTree(['/login']);
  }
}