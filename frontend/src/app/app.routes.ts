import { Routes } from '@angular/router';

import { AuthGuard } from './auth/auth.guard';
import { RoleGuard } from './auth/role.guard';

export const routes: Routes = [
  // Public
  { path: '', loadComponent: () => import('./pages/landing/landing.component').then(m => m.LandingComponent) },
  { path: 'login', loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'logout', loadComponent: () => import('./auth/logout/logout.component').then(m => m.LogoutComponent) },
  { path: 'auth/callback', loadComponent: () => import('./pages/auth-callback/auth-callback.component').then(m => m.AuthCallbackComponent) },

  // ── Admin ──────────────────────────────────────────────────────────────────
  {
    path: 'admin',
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ADMIN'] },
    loadChildren: () => import('./modules/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },

  // ── Doctor ─────────────────────────────────────────────────────────────────
  {
    path: 'doctor',
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_DOCTOR'] },
    loadChildren: () => import('./modules/doctor/doctor.routes').then(m => m.DOCTOR_ROUTES)
  },

  // ── Nurse ──────────────────────────────────────────────────────────────────
  {
    path: 'nurse',
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_NURSE'] },
    loadChildren: () => import('./modules/nurse/nurse.routes').then(m => m.NURSE_ROUTES)
  },

  { path: '**', redirectTo: '' }
];