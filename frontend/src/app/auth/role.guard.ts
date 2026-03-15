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

    const roles = new Set(this.auth.roles());
    const ok = required.some((r) => roles.has(r));
    return ok ? true : this.router.createUrlTree(['/login']);
  }
}