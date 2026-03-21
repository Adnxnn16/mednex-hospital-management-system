import { Routes } from '@angular/router';
import { AdminDashboardComponent } from './dashboard/dashboard.component';
import { AnalyticsComponent } from './analytics/analytics.component';
import { InventoryComponent } from './inventory/inventory.component';
import { SettingsComponent } from './settings/settings.component';
import { PatientsComponent } from '../../pages/patients/patients.component';
import { PatientDetailComponent } from '../../pages/patients/patient-detail.component';
import { DoctorsComponent } from '../../pages/doctors/doctors.component';
import { AppointmentsComponent } from '../../pages/appointments/appointments.component';
import { AuditComponent } from '../../pages/audit/audit.component';

export const ADMIN_ROUTES: Routes = [
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
];
