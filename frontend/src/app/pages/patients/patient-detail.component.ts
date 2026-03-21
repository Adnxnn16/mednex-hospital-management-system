import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService, PatientDto } from '../../shared/api.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
<div class="patient-detail-page">
  <div class="header-actions">
    <a [routerLink]="['/doctor/patients']" class="back-link">
      <span class="material-symbols-outlined">arrow_back</span> Back to Patients
    </a>
  </div>

  <div *ngIf="loading" class="loading-state">
    <div class="spinner"></div>
    <p>Loading patient records...</p>
  </div>

  <div *ngIf="patient && !loading" class="content-grid">
    <!-- Left Column: Patient Info & Timeline -->
    <div class="left-col">
      <div class="pg-card patient-header-card">
        <div class="avatar">{{ patient.firstName[0] }}{{ patient.lastName[0] }}</div>
        <div class="info">
          <h2>{{ patient.firstName }} {{ patient.lastName }}</h2>
          <div class="meta">
            <span><span class="material-symbols-outlined">calendar_today</span> {{ patient.dob | date }}</span>
            <span><span class="material-symbols-outlined">wc</span> {{ patient.gender }}</span>
            <span><span class="material-symbols-outlined">bloodtype</span> {{ patient.bloodGroup || 'Unknown' }}</span>
          </div>
          <div class="contact-badges">
            <span class="badge" *ngIf="patient.phone"><span class="material-symbols-outlined">call</span> {{ patient.phone }}</span>
            <span class="badge" *ngIf="patient.email"><span class="material-symbols-outlined">mail</span> {{ patient.email }}</span>
          </div>
        </div>
      </div>

      <div class="pg-card timeline-card">
        <div class="card-title">
          <span class="material-symbols-outlined">history</span> Patient History Timeline
        </div>
        
        <div class="timeline" *ngIf="consultations.length > 0">
          <div class="timeline-item" *ngFor="let c of consultations">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="time-header">
                <span class="date">{{ c.date | date:'MMM d, yyyy - h:mm a' }}</span>
                <span class="doc">Dr. {{ c.doctorName }}</span>
              </div>
              
              <div class="clinical-block" *ngIf="c.symptoms">
                <strong>Symptoms:</strong> {{ c.symptoms }}
              </div>
              <div class="clinical-block" *ngIf="c.diagnosis">
                <strong>Diagnosis:</strong> {{ c.diagnosis }}
              </div>
              <div class="clinical-block" *ngIf="c.treatment">
                <strong>Treatment/Prescription:</strong> {{ c.treatment }}
              </div>
              <div class="clinical-block notes" *ngIf="c.notes">
                <em>"{{ c.notes }}"</em>
              </div>
            </div>
          </div>
        </div>
        
        <div *ngIf="consultations.length === 0" class="empty-timeline">
          <span class="material-symbols-outlined">clinical_notes</span>
          <p>No previous medical history recorded.</p>
        </div>
      </div>
    </div>

    <!-- Right Column: Add Consultation Form (Doctor Only) -->
    <div class="right-col">
      <div class="pg-card add-consult-card" *ngIf="auth.roles().includes('ROLE_DOCTOR')">
        <div class="card-title">
          <span class="material-symbols-outlined">edit_note</span> Add Consultation Note
        </div>
        
        <form [formGroup]="consultForm" (ngSubmit)="submitConsultation()">
          <div class="form-group">
            <label>Observed Symptoms <span class="req">*</span></label>
            <textarea formControlName="symptoms" rows="3" placeholder="E.g., Patient presents with a 3-day history of..."></textarea>
          </div>
          
          <div class="form-group">
            <label>Diagnosis <span class="req">*</span></label>
            <input formControlName="diagnosis" type="text" placeholder="ICD-10 or clinical diagnosis">
          </div>
          
          <div class="form-group">
            <label>Treatment & Prescriptions</label>
            <textarea formControlName="treatment" rows="3" placeholder="Prescribed medications, dosage..."></textarea>
          </div>
          
          <div class="form-group">
            <label>Private Clinical Notes</label>
            <textarea formControlName="notes" rows="2" placeholder="Additional observations..."></textarea>
          </div>

          <button type="submit" class="submit-btn" [disabled]="consultForm.invalid || submitting">
            <span class="material-symbols-outlined" *ngIf="!submitting">save</span>
            <div class="spinner-small" *ngIf="submitting"></div>
            {{ submitting ? 'Saving...' : 'Save Record' }}
          </button>
        </form>
      </div>
      
      <div class="pg-card" *ngIf="!auth.roles().includes('ROLE_DOCTOR')">
        <div class="alert warn">
          <span class="material-symbols-outlined">lock</span>
          <p>Only authorized physicians can append to the patient medical record.</p>
        </div>
      </div>
    </div>
  </div>
</div>
  `,
  styles: [`:host{display:block}.patient-detail-page{padding:16px}`]
})
export class PatientDetailComponent implements OnInit {
  patientId!: string;
  patient: PatientDto | null = null;
  consultations: any[] = [];
  loading = true;
  submitting = false;

  consultForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private api: ApiService,
    public auth: AuthService,
    private fb: FormBuilder,
    private snack: MatSnackBar
  ) {
    this.consultForm = this.fb.group({
      symptoms: ['', Validators.required],
      diagnosis: ['', Validators.required],
      treatment: [''],
      notes: ['']
    });
  }

  ngOnInit() {
    this.patientId = this.route.snapshot.paramMap.get('id')!;
    this.loadPatient();
  }

  loadPatient() {
    this.loading = true;
    this.api.getPatient(this.patientId).subscribe({
      next: (p) => {
        this.patient = p;
        try {
          const history = JSON.parse(p.medicalHistory || '{}');
          this.consultations = history.consultations || [];
          // Sort descending by date
          this.consultations.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
        } catch (e) {
          this.consultations = [];
        }
        this.loading = false;
      },
      error: () => {
        this.snack.open('Failed to load patient data', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  submitConsultation() {
    if (this.consultForm.invalid) return;

    this.submitting = true;
    const payload = {
      ...this.consultForm.value,
      date: new Date().toISOString(),
      doctorName: this.auth.username()
    };

    this.api.addConsultation(this.patientId, payload).subscribe({
      next: () => {
        this.snack.open('Consultation saved successfully', 'Close', { duration: 3000 });
        this.consultForm.reset();
        this.submitting = false;
        this.loadPatient(); // Reload to show the new note in the timeline
      },
      error: () => {
        this.snack.open('Failed to save consultation', 'Close', { duration: 3000 });
        this.submitting = false;
      }
    });
  }
}
