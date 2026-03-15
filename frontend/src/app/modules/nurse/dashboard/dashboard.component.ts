import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService, BedDto, PatientDto, AdmissionDto } from '../../../shared/api.service';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-nurse-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
<div class="nurse-dash">

  <!-- Welcome Banner -->
  <div class="welcome-banner">
    <div class="welcome-text">
      <h2>{{ greeting }}, {{ auth.username() }}</h2>
      <p>Ward status and admission overview</p>
    </div>
    <div class="quick-stats">
      <div class="qstat blue">
        <span class="qstat-val">{{ totalBeds }}</span>
        <span class="qstat-lbl">Total Beds</span>
      </div>
      <div class="qstat red">
        <span class="qstat-val">{{ occupiedBeds }}</span>
        <span class="qstat-lbl">Occupied</span>
      </div>
      <div class="qstat green">
        <span class="qstat-val">{{ availableBeds }}</span>
        <span class="qstat-lbl">Available</span>
      </div>
    </div>
  </div>

  <div class="nurse-grid">

    <!-- Bed Status Overview -->
    <div class="dash-card bed-card">
      <div class="card-head">
        <span class="material-symbols-outlined">hotel</span>
        <h3>Bed Status</h3>
        <a [routerLink]="['/nurse/beds']" class="view-all">Manage Beds</a>
      </div>
      <div *ngIf="loading" class="shimmer-col">
        <div class="shimmer" *ngFor="let i of [1,2,3,4]"></div>
      </div>
      <div class="bed-grid" *ngIf="!loading">
        <div *ngFor="let bed of beds" class="bed-tile" [class]="'bed-' + bed.status.toLowerCase()">
          <span class="material-symbols-outlined">{{ bed.status === 'AVAILABLE' ? 'bed' : 'personal_injury' }}</span>
          <div class="bed-label">{{ bed.ward }}</div>
          <div class="bed-num">{{ bed.room }}-{{ bed.bedNumber }}</div>
          <div class="bed-status-badge">{{ bed.status }}</div>
        </div>
      </div>
    </div>

    <!-- Occupancy Bar -->
    <div class="dash-card">
      <div class="card-head">
        <span class="material-symbols-outlined">bar_chart</span>
        <h3>Occupancy Rate</h3>
      </div>
      <div class="occupancy-ring-wrapper">
        <div class="occupancy-circle">
          <svg viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="40" fill="none" stroke="rgba(255,255,255,0.05)" stroke-width="12"/>
            <circle cx="50" cy="50" r="40" fill="none"
              stroke="#22A3FF" stroke-width="12"
              stroke-dasharray="251.2"
              [attr.stroke-dashoffset]="251.2 - (occupancyRate / 100) * 251.2"
              stroke-linecap="round"
              transform="rotate(-90 50 50)"/>
          </svg>
          <div class="ring-label">
            <span class="ring-pct">{{ occupancyRate }}%</span>
            <span class="ring-sub">Occupancy</span>
          </div>
        </div>
      </div>
      <div class="occ-legend">
        <div class="leg-item"><span class="dot red"></span> Occupied ({{ occupiedBeds }})</div>
        <div class="leg-item"><span class="dot green"></span> Available ({{ availableBeds }})</div>
      </div>
    </div>

    <!-- Patient Ward List & Vitals Tracker -->
    <div class="dash-card ward-card">
      <div class="card-head">
        <span class="material-symbols-outlined">monitor_heart</span>
        <h3>Active Ward Patients & Vitals</h3>
      </div>

      <!-- Vitals Input Form -->
      <div *ngIf="selectedAdmission" class="vitals-form-box">
        <div class="vitals-header">
          <h4>Record Vitals for {{ getPatientName(selectedAdmission.patientId) }}</h4>
          <button (click)="selectedAdmission = null" class="icon-btn close-btn"><span class="material-symbols-outlined">close</span></button>
        </div>
        <form [formGroup]="vitalsForm" (ngSubmit)="submitVitals()">
          <div class="grid-2">
            <div class="form-group">
              <label>Blood Pressure</label>
              <input formControlName="bloodPressure" type="text" placeholder="120/80">
            </div>
            <div class="form-group">
              <label>Heart Rate</label>
              <input formControlName="heartRate" type="text" placeholder="72 bpm">
            </div>
            <div class="form-group">
              <label>Temperature</label>
              <input formControlName="temperature" type="text" placeholder="98.6 F">
            </div>
            <div class="form-group">
              <label>Oxygen Level (SPO2)</label>
              <input formControlName="oxygenLevel" type="text" placeholder="99%">
            </div>
          </div>
          <button type="submit" [disabled]="vitalsForm.invalid || submittingVitals" class="submit-btn">
            {{ submittingVitals ? 'Saving...' : 'Save Vitals Record' }}
          </button>
        </form>
      </div>

      <!-- Admissions List -->
      <div class="patient-list" *ngIf="!selectedAdmission">
        <div *ngIf="activeAdmissions.length === 0" class="empty-state">No patients currently admitted.</div>
        <div *ngFor="let adm of activeAdmissions" class="patient-row">
          <div class="patient-avatar">{{ getPatientName(adm.patientId)[0] }}</div>
          <div class="patient-info">
            <span class="patient-name">{{ getPatientName(adm.patientId) }}</span>
            <span class="patient-sub">Bed: {{ getBedLabel(adm.bedId) }}</span>
          </div>
          <div class="vitals">
            <ng-container *ngIf="getLatestVitals(adm) as v; else novitals">
               <span class="vital-chip">BP: {{ v.bloodPressure }}</span>
               <span class="vital-chip">O₂: {{ v.oxygenLevel }}</span>
            </ng-container>
            <ng-template #novitals>
               <span class="vital-chip no-data">No vitals yet</span>
            </ng-template>
          </div>
          <button class="icon-btn record-vitals-btn" (click)="selectedAdmission = adm" title="Record Vitals">
            <span class="material-symbols-outlined">add_chart</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="dash-card actions-card">
      <div class="card-head">
        <span class="material-symbols-outlined">bolt</span>
        <h3>Quick Actions</h3>
      </div>
      <div class="action-list">
        <a [routerLink]="['/nurse/admissions']" class="action-btn blue">
          <span class="material-symbols-outlined">add_circle</span>
          New Admission
        </a>
        <a [routerLink]="['/nurse/beds']" class="action-btn green">
          <span class="material-symbols-outlined">hotel</span>
          Update Bed Status
        </a>
        <a [routerLink]="['/nurse/admissions']" class="action-btn amber">
          <span class="material-symbols-outlined">logout</span>
          Discharge Patient
        </a>
      </div>
    </div>

  </div>
</div>
  `,
  styles: [`
    .nurse-dash { padding: 28px; }

    .welcome-banner {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: linear-gradient(135deg, rgba(34,197,94,0.10), rgba(16,185,129,0.06));
      border: 1px solid rgba(34,197,94,0.2);
      border-radius: 20px;
      padding: 28px 32px;
      margin-bottom: 28px;
    }
    .welcome-text h2 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: white; }
    .welcome-text p  { margin: 0; color: #64748B; font-size: 14px; }

    .quick-stats { display: flex; gap: 28px; }
    .qstat { text-align: center; }
    .qstat-val { display: block; font-size: 32px; font-weight: 800; }
    .qstat-lbl { font-size: 12px; color: #64748B; font-weight: 500; }
    .qstat.blue .qstat-val { color: #22A3FF; }
    .qstat.red  .qstat-val { color: #f87171; }
    .qstat.green .qstat-val { color: #22c55e; }

    .nurse-grid {
      display: grid;
      grid-template-columns: 1fr 220px;
      grid-template-rows: auto auto;
      gap: 20px;
    }
    .bed-card, .ward-card { grid-column: 1; }

    .dash-card {
      background: #111827;
      border: 1px solid rgba(255,255,255,0.07);
      border-radius: 16px;
      padding: 20px;
    }
    .card-head {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 16px;
      .material-symbols-outlined { color: #22c55e; font-size: 20px; }
      h3 { margin: 0; font-size: 15px; font-weight: 600; color: white; flex: 1; }
      .view-all { font-size: 12px; color: #22A3FF; text-decoration: none; font-weight: 600; }
    }

    /* Bed Grid */
    .bed-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(90px, 1fr)); gap: 10px; }
    .bed-tile {
      border-radius: 12px;
      padding: 12px 8px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      border: 1px solid transparent;
      .material-symbols-outlined { font-size: 22px; }
      .bed-label { font-size: 10px; color: #94A3B8; }
      .bed-num   { font-weight: 700; color: white; font-size: 12px; }
      .bed-status-badge { font-size: 9px; font-weight: 700; padding: 2px 6px; border-radius: 6px; }
    }
    .bed-available { background: rgba(34,197,94,0.08); border-color: rgba(34,197,94,0.2); .material-symbols-outlined { color: #22c55e; } .bed-status-badge { background: rgba(34,197,94,0.15); color: #22c55e; } }
    .bed-occupied  { background: rgba(248,113,113,0.08); border-color: rgba(248,113,113,0.2); .material-symbols-outlined { color: #f87171; } .bed-status-badge { background: rgba(248,113,113,0.15); color: #f87171; } }
    .bed-cleaning  { background: rgba(251,191,36,0.08); border-color: rgba(251,191,36,0.2); .material-symbols-outlined { color: #fbbf24; } .bed-status-badge { background: rgba(251,191,36,0.15); color: #fbbf24; } }

    /* Occupancy Ring */
    .occupancy-ring-wrapper { display: flex; justify-content: center; padding: 12px 0; }
    .occupancy-circle { position: relative; width: 120px; height: 120px; }
    .occupancy-circle svg { width: 100%; height: 100%; }
    .ring-label { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); text-align: center; }
    .ring-pct { display: block; font-size: 22px; font-weight: 800; color: white; }
    .ring-sub { font-size: 10px; color: #64748B; }
    .occ-legend { display: flex; flex-direction: column; gap: 6px; margin-top: 12px; }
    .leg-item  { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #94A3B8; }
    .dot { width: 8px; height: 8px; border-radius: 50%; }
    .dot.red   { background: #f87171; }
    .dot.green { background: #22c55e; }

    /* Patient List */
    .patient-list { display: flex; flex-direction: column; gap: 6px; }
    .patient-row { display: flex; align-items: center; gap: 12px; padding: 10px; border-radius: 10px; transition: background 0.15s; border: 1px solid transparent; }
    .patient-row:hover { background: rgba(255,255,255,0.03); border-color: rgba(255,255,255,0.05); }
    .patient-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg,#22c55e,#0d9488); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; color: white; flex-shrink: 0; }
    .patient-info { flex: 1; }
    .patient-name { display: block; font-size: 13px; font-weight: 600; color: white; }
    .patient-sub  { font-size: 11px; color: #64748B; }
    .vitals { display: flex; gap: 4px; flex-wrap: wrap; }
    .vital-chip { font-size: 10px; font-weight: 600; padding: 3px 7px; background: rgba(34,163,255,0.1); color: #22A3FF; border-radius: 6px; border: 1px solid rgba(34,163,255,0.2) }
    .vital-chip.no-data { background: transparent; border-color: transparent; color: #64748B; font-weight: normal; }
    .record-vitals-btn { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: rgba(34,197,94,0.1); color: #22c55e; border-radius: 8px; border: 1px solid rgba(34,197,94,0.2); transition: all 0.2s; cursor: pointer; }
    .record-vitals-btn:hover { background: #22c55e; color: white; }

    /* Vitals Form Box */
    .vitals-form-box { background: rgba(0,0,0,0.2); border: 1px solid rgba(34,197,94,0.3); border-radius: 12px; padding: 20px; }
    .vitals-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .vitals-header h4 { margin: 0; font-size: 14px; font-weight: 600; color: white; }
    .close-btn { background: none; border: none; color: #94A3B8; cursor: pointer; font-size: 18px; }
    .close-btn:hover { color: white; }
    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px; }
    .form-group label { display: block; font-size: 11px; font-weight: 600; color: #94A3B8; margin-bottom: 4px; text-transform: uppercase; }
    .form-group input { width: 100%; padding: 10px; background: #111827; border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; color: white; font-size: 13px; box-sizing: border-box; }
    .form-group input:focus { outline: none; border-color: #22c55e; }
    .submit-btn { width: 100%; padding: 12px; background: #22c55e; color: white; border: none; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; transition: background 0.2s; }
    .submit-btn:hover:not(:disabled) { background: #16a34a; }
    .submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

    /* Quick Actions */
    .action-list { display: flex; flex-direction: column; gap: 10px; }
    .action-btn {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 16px;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 600;
      text-decoration: none;
      transition: all 0.2s;
      .material-symbols-outlined { font-size: 18px; }
    }
    .action-btn.blue  { background: rgba(34,163,255,0.1); color: #22A3FF; border: 1px solid rgba(34,163,255,0.2); }
    .action-btn.green { background: rgba(34,197,94,0.1);  color: #22c55e; border: 1px solid rgba(34,197,94,0.2); }
    .action-btn.amber { background: rgba(245,158,11,0.1); color: #f59e0b; border: 1px solid rgba(245,158,11,0.2); }
    .action-btn:hover { transform: translateX(4px); }

    /* Shimmer */
    .shimmer-col { display: flex; flex-direction: column; gap: 8px; }
    .shimmer { height: 80px; border-radius: 12px; background: linear-gradient(90deg,rgba(255,255,255,0.04) 25%, rgba(255,255,255,0.08) 50%, rgba(255,255,255,0.04) 75%); background-size: 200% 100%; animation: shimmer 1.2s infinite; }
    @keyframes shimmer { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
    .empty-state { text-align: center; color: #64748B; font-size: 13px; padding: 20px; }
  `]
})
export class NurseDashboardComponent implements OnInit {
  loading = true;
  greeting = 'Good Morning';
  beds: BedDto[] = [];
  patients: PatientDto[] = [];
  activeAdmissions: AdmissionDto[] = [];
  
  totalBeds = 0;
  occupiedBeds = 0;
  availableBeds = 0;
  occupancyRate = 0;

  selectedAdmission: AdmissionDto | null = null;
  vitalsForm: FormGroup;
  submittingVitals = false;

  constructor(
    private api: ApiService, 
    public auth: AuthService,
    private fb: FormBuilder,
    private snack: MatSnackBar
  ) {
    this.vitalsForm = this.fb.group({
      bloodPressure: ['', Validators.required],
      heartRate: ['', Validators.required],
      temperature: ['', Validators.required],
      oxygenLevel: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const h = new Date().getHours();
    this.greeting = h < 12 ? 'Good Morning' : h < 17 ? 'Good Afternoon' : 'Good Evening';
    this.loadData();
  }

  loadData() {
    this.api.listBeds().subscribe(b => {
      this.beds = b.slice(0, 20); // Show first 20 beds in grid
      this.totalBeds = b.length;
      this.occupiedBeds = b.filter(bed => bed.status === 'OCCUPIED').length;
      this.availableBeds = b.filter(bed => bed.status === 'AVAILABLE').length;
      this.occupancyRate = this.totalBeds > 0
        ? Math.round((this.occupiedBeds / this.totalBeds) * 100)
        : 0;
      this.loading = false;
    });

    this.api.listPatients().subscribe(p => this.patients = p);
    
    this.api.listAdmissions().subscribe(a => {
      // Only keep currently admitted patients
      this.activeAdmissions = a.filter(adm => !adm.dischargedAt);
    });
  }

  getPatientName(id: string): string {
    const p = this.patients.find(x => x.id === id);
    return p ? `${p.firstName} ${p.lastName}` : 'Unknown';
  }

  getBedLabel(id: string): string {
    const b = this.beds.find(x => x.id === id);
    return b ? `${b.ward} ${b.room}-${b.bedNumber}` : 'Unknown Bed';
  }

  getLatestVitals(adm: AdmissionDto): any {
    try {
      if (!adm.vitals) return null;
      const vArray = JSON.parse(adm.vitals);
      if (Array.isArray(vArray) && vArray.length > 0) {
        // Return latest (last element)
        return vArray[vArray.length - 1];
      }
    } catch {}
    return null;
  }

  submitVitals() {
    if (this.vitalsForm.invalid || !this.selectedAdmission) return;
    
    this.submittingVitals = true;
    this.api.addVitals(this.selectedAdmission.id, this.vitalsForm.value).subscribe({
      next: (updatedAdm) => {
        this.snack.open('Vitals recorded successfully!', 'Close', { duration: 3000 });
        this.selectedAdmission = null;
        this.vitalsForm.reset();
        this.submittingVitals = false;
        
        // Update local arrays
        const idx = this.activeAdmissions.findIndex(a => a.id === updatedAdm.id);
        if (idx !== -1) {
          this.activeAdmissions[idx] = updatedAdm;
        }
      },
      error: () => {
        this.snack.open('Error saving vitals', 'Close', { duration: 3000 });
        this.submittingVitals = false;
      }
    });
  }
}
