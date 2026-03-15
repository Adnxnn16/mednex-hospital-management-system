import { Routes } from '@angular/router';

import { AuthGuard } from './auth/auth.guard';
import { RoleGuard } from './auth/role.guard';
import { LoginComponent } from './auth/login/login.component';
import { ShellComponent } from './layout/shell/shell.component';
import { LandingComponent } from './pages/landing/landing.component';
import { AuthCallbackComponent } from './pages/auth-callback/auth-callback.component';

import { AdminDashboardComponent } from './modules/admin/dashboard/dashboard.component';
import { DoctorDashboardComponent } from './modules/doctor/dashboard/dashboard.component';
import { NurseDashboardComponent } from './modules/nurse/dashboard/dashboard.component';

import { PatientsComponent } from './pages/patients/patients.component';
import { PatientDetailComponent } from './pages/patients/patient-detail.component';
import { AppointmentsComponent } from './pages/appointments/appointments.component';
import { BedsComponent } from './pages/beds/beds.component';
import { AdmissionComponent } from './pages/admission/admission.component';
import { DoctorsComponent } from './pages/doctors/doctors.component';
import { AuditComponent } from './pages/audit/audit.component';
import { AnalyticsComponent } from './modules/admin/analytics/analytics.component';
import { InventoryComponent } from './modules/admin/inventory/inventory.component';
import { SettingsComponent } from './modules/admin/settings/settings.component';

export const routes: Routes = [
  // Public
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'auth/callback', component: AuthCallbackComponent },

  // ── Admin ──────────────────────────────────────────────────────────────────
  {
    path: 'admin',
    component: ShellComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ADMIN'] },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard',    component: AdminDashboardComponent },
      { path: 'patients',     component: PatientsComponent },
      { path: 'patients/:id', component: PatientDetailComponent },
      { path: 'doctors',      component: DoctorsComponent },
      { path: 'appointments', component: AppointmentsComponent },
      { path: 'audit',        component: AuditComponent },
      { path: 'analytics',    component: AnalyticsComponent },
      { path: 'inventory',    component: InventoryComponent },
      { path: 'settings',     component: SettingsComponent },
    ]
  },

  // ── Doctor ─────────────────────────────────────────────────────────────────
  {
    path: 'doctor',
    component: ShellComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_DOCTOR'] },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard',    component: DoctorDashboardComponent },
      { path: 'appointments', component: AppointmentsComponent },
      { path: 'patients',     component: PatientsComponent },
      { path: 'patients/:id', component: PatientDetailComponent },
      { path: 'settings',     component: SettingsComponent },
    ]
  },

  // ── Nurse ──────────────────────────────────────────────────────────────────
  {
    path: 'nurse',
    component: ShellComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_NURSE'] },
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard',  component: NurseDashboardComponent },
      { path: 'admissions', component: AdmissionComponent },
      { path: 'beds',       component: BedsComponent },
      { path: 'patients',   component: PatientsComponent },
      { path: 'patients/:id', component: PatientDetailComponent },
      { path: 'settings',   component: SettingsComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];