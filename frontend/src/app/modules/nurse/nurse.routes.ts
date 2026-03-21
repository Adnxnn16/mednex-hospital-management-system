import { Routes } from '@angular/router';
import { NurseDashboardComponent } from './dashboard/dashboard.component';
import { AdmissionComponent } from '../../pages/admission/admission.component';
import { BedsComponent } from '../../pages/beds/beds.component';
import { AppointmentsComponent } from '../../pages/appointments/appointments.component';
import { PatientsComponent } from '../../pages/patients/patients.component';
import { PatientDetailComponent } from '../../pages/patients/patient-detail.component';
import { SettingsComponent } from '../admin/settings/settings.component';

export const NURSE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard',  component: NurseDashboardComponent },
  { path: 'admissions', component: AdmissionComponent },
  { path: 'appointments', component: AppointmentsComponent },
  { path: 'beds',       component: BedsComponent },
  { path: 'patients',   component: PatientsComponent },
  { path: 'patients/:id', component: PatientDetailComponent },
  { path: 'settings',   component: SettingsComponent },
];
