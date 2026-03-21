import { Routes } from '@angular/router';
import { DoctorDashboardComponent } from './dashboard/dashboard.component';
import { AppointmentsComponent } from '../../pages/appointments/appointments.component';
import { PatientsComponent } from '../../pages/patients/patients.component';
import { PatientDetailComponent } from '../../pages/patients/patient-detail.component';
import { SettingsComponent } from '../admin/settings/settings.component';

export const DOCTOR_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard',    component: DoctorDashboardComponent },
  { path: 'appointments', component: AppointmentsComponent },
  { path: 'patients',     component: PatientsComponent },
  { path: 'patients/:id', component: PatientDetailComponent },
  { path: 'settings',     component: SettingsComponent },
];
