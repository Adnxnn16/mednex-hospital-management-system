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
  styles: [`
    .patient-detail-page { padding: 24px; max-width: 1400px; margin: 0 auto; }
    
    .header-actions { margin-bottom: 24px; }
    .back-link { display: inline-flex; align-items: center; gap: 8px; color: #94A3B8; text-decoration: none; font-size: 14px; font-weight: 500; transition: color 0.2s; }
    .back-link:hover { color: #fff; }
    .back-link .material-symbols-outlined { font-size: 18px; }

    .content-grid { display: grid; grid-template-columns: 1fr 450px; gap: 24px; align-items: start; }
    
    .pg-card { background: #111827; border: 1px solid rgba(255,255,255,0.07); border-radius: 16px; padding: 24px; margin-bottom: 24px; }
    .card-title { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 600; color: white; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid rgba(255,255,255,0.05); }
    .card-title .material-symbols-outlined { color: #22A3FF; font-size: 20px; }

    /* Patient Header */
    .patient-header-card { display: flex; gap: 24px; align-items: center; background: linear-gradient(135deg, rgba(34,163,255,0.08), rgba(99,102,241,0.05)); border: 1px solid rgba(34,163,255,0.15); }
    .avatar { width: 80px; height: 80px; border-radius: 20px; background: linear-gradient(135deg, #22A3FF, #6366f1); display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; color: white; box-shadow: 0 8px 16px rgba(34,163,255,0.2); }
    .info h2 { margin: 0 0 8px; font-size: 24px; font-weight: 700; color: white; }
    .meta { display: flex; gap: 16px; margin-bottom: 12px; }
    .meta span { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #94A3B8; }
    .meta span .material-symbols-outlined { font-size: 16px; color: #64748B; }
    .contact-badges { display: flex; gap: 8px; }
    .badge { display: flex; align-items: center; gap: 6px; padding: 4px 10px; background: rgba(255,255,255,0.05); border-radius: 8px; font-size: 12px; color: #cbd5e1; }
    .badge .material-symbols-outlined { font-size: 14px; color: #22A3FF; }

    /* Timeline */
    .timeline { position: relative; padding-left: 20px; }
    .timeline::before { content: ''; position: absolute; left: 6px; top: 8px; bottom: 0; width: 2px; background: rgba(255,255,255,0.05); }
    .timeline-item { position: relative; padding-bottom: 32px; }
    .timeline-item:last-child { padding-bottom: 0; }
    .timeline-dot { position: absolute; left: -20px; top: 4px; width: 14px; height: 14px; border-radius: 50%; background: #22A3FF; border: 3px solid #111827; box-shadow: 0 0 0 2px rgba(34,163,255,0.2); }
    .timeline-content { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); border-radius: 12px; padding: 16px; }
    .time-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .date { font-size: 13px; font-weight: 600; color: #cbd5e1; }
    .doc { font-size: 12px; color: #22A3FF; background: rgba(34,163,255,0.1); padding: 2px 8px; border-radius: 6px; }
    .clinical-block { font-size: 13px; color: #94A3B8; margin-bottom: 8px; line-height: 1.5; }
    .clinical-block strong { color: #e2e8f0; font-weight: 600; }
    .clinical-block.notes { font-style: italic; color: #64748B; margin-top: 12px; padding-top: 12px; border-top: 1px dashed rgba(255,255,255,0.05); }
    
    .empty-timeline { text-align: center; padding: 40px 20px; color: #64748B; }
    .empty-timeline .material-symbols-outlined { font-size: 48px; opacity: 0.5; margin-bottom: 16px; }

    /* Form */
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; font-size: 13px; font-weight: 500; color: #cbd5e1; margin-bottom: 8px; }
    .req { color: #f87171; }
    input[type="text"], textarea { width: 100%; padding: 12px 16px; background: rgba(0,0,0,0.2); border: 1px solid rgba(255,255,255,0.1); border-radius: 10px; color: white; font-family: inherit; font-size: 14px; transition: border-color 0.2s; resize: vertical; box-sizing: border-box; }
    input:focus, textarea:focus { outline: none; border-color: #22A3FF; }
    
    .submit-btn { width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 14px; background: #22A3FF; color: white; border: none; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
    .submit-btn:hover:not(:disabled) { background: #1b8bd9; }
    .submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

    .loading-state { text-align: center; padding: 60px 20px; color: #94A3B8; }
    .spinner { display: inline-block; width: 30px; height: 30px; border: 3px solid rgba(34,163,255,0.2); border-top-color: #22A3FF; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 16px; }
    .spinner-small { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .alert.warn { display: flex; gap: 12px; background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.2); padding: 16px; border-radius: 12px; color: #f59e0b; }
    .alert.warn p { margin: 0; font-size: 13px; line-height: 1.5; }
    
    @media (max-width: 1024px) {
      .content-grid { grid-template-columns: 1fr; }
    }
  `]
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
