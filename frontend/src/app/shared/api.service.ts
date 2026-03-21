import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';

export type PatientDto = {
  id: string;
  firstName: string;
  lastName: string;
  dob: string | null;
  gender: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
  bloodGroup: string | null;
  occupation: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
  insuranceProvider: string | null;
  policyNumber: string | null;
  medicalHistory: string;
  createdAt: string;
  updatedAt: string;
};

export type DoctorDto = {
  id: string;
  firstName: string;
  lastName: string;
  specialisation: string;
  licenceNumber: string;
  tenantId: string | null;
  email: string | null;
  createdAt: string;
  /** Computed display name (backend may omit; fallback: firstName + ' ' + lastName) */
  fullName?: string;
  /** Alias for specialisation (backward compat) */
  specialty?: string;
};

export type BedDto = {
  id: string;
  ward: string;
  room: string;
  bedNumber: string;
  status: string;
  createdAt: string;
};

export type BedOccupancyDto = {
  tenantId: string;
  totalBeds: number;
  occupiedBeds: number;
  occupancyRate: number;
  wards: WardOccupancyItemDto[];
};

export type WardOccupancyItemDto = {
  wardName: string;
  totalBeds: number;
  occupiedBeds: number;
  occupancyRate: number;
};

export type AdmissionDto = {
  id: string;
  patientId: string;
  bedId: string;
  admittedAt: string;
  dischargedAt: string | null;
  notes: string | null;
  vitals: string;
};

export type AppointmentDto = {
  id: string;
  patientId: string;
  doctorId: string;
  startTime: string;
  endTime: string;
  status: string;
  createdAt: string;
};

export type AuditLogDto = {
  id: string;
  tenantId: string;
  userId: string | null;
  action: string;
  entityType: string | null;
  entityId: string | null;
  occurredAt: string;
  metadata: string;
};

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiBase;

  constructor(private readonly http: HttpClient) {}

  bedOccupancy() {
    return this.http.get<BedOccupancyDto>(`${this.base}/api/analytics/bed-occupancy`);
  }

  listPatients() {
    return this.http.get<PatientDto[]>(`${this.base}/api/patients`);
  }

  createPatient(payload: {
    firstName: string;
    lastName: string;
    dob?: string | null;
    gender?: string | null;
    email?: string | null;
    phone?: string | null;
    address?: string | null;
    bloodGroup?: string | null;
    occupation?: string | null;
    emergencyContactName?: string | null;
    emergencyContactPhone?: string | null;
    insuranceProvider?: string | null;
    policyNumber?: string | null;
    medicalHistory?: string | null;
  }) {
    return this.http.post<PatientDto>(`${this.base}/api/patients`, payload);
  }

  listDoctors() {
    return this.http.get<DoctorDto[]>(`${this.base}/api/doctors`);
  }

  createDoctor(payload: {
    firstName: string;
    lastName: string;
    specialisation: string;
    licenceNumber: string;
    email?: string | null;
  }) {
    return this.http.post<DoctorDto>(`${this.base}/api/doctors`, payload);
  }

  listBeds() {
    return this.http.get<BedDto[]>(`${this.base}/api/beds`);
  }

  updateBedStatus(id: string, status: string) {
    return this.http.patch<BedDto>(`${this.base}/api/beds/${id}/status`, { status });
  }

  listAppointments() {
    return this.http.get<AppointmentDto[]>(`${this.base}/api/appointments`);
  }

  createAppointment(payload: {
    patientId: string;
    doctorId: string;
    startTime: string;
    endTime: string;
  }) {
    return this.http.post<AppointmentDto>(`${this.base}/api/appointments`, payload);
  }

  admitPatient(payload: {
    patient: {
      firstName: string;
      lastName: string;
      dob?: string | null;
      gender?: string | null;
      email?: string | null;
      phone?: string | null;
      address?: string | null;
      bloodGroup?: string | null;
      occupation?: string | null;
      emergencyContactName?: string | null;
      emergencyContactPhone?: string | null;
      insuranceProvider?: string | null;
      policyNumber?: string | null;
      medicalHistory?: string | null;
    };
    bedId: string;
    notes?: string | null;
  }) {
    return this.http.post<AdmissionDto>(`${this.base}/api/admissions`, payload);
  }

  auditLog() {
    return this.http.get<AuditLogDto[]>(`${this.base}/api/audit-log`);
  }

  getPatient(id: string) {
    return this.http.get<PatientDto>(`${this.base}/api/patients/${id}`);
  }

  addConsultation(patientId: string, payload: {
    date: string;
    doctorName: string;
    symptoms: string;
    diagnosis: string;
    treatment: string;
    notes: string;
  }) {
    return this.http.post<PatientDto>(`${this.base}/api/patients/${patientId}/consultations`, payload);
  }

  listAdmissions() {
    return this.http.get<AdmissionDto[]>(`${this.base}/api/admissions`);
  }

  addVitals(admissionId: string, payload: {
    bloodPressure: string;
    heartRate: string;
    temperature: string;
    oxygenLevel: string;
  }) {
    return this.http.post<AdmissionDto>(`${this.base}/api/admissions/${admissionId}/vitals`, payload);
  }
}