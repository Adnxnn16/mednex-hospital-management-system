import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormArray } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-medical-history',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatExpansionModule,
    MatChipsModule,
    MatSelectModule
  ],

  template: `
    <div [formGroup]="form" class="medical-history">
      <mat-expansion-panel [expanded]="true">
        <mat-expansion-panel-header>
          <mat-panel-title>Vitals & Clinical Metrics</mat-panel-title>
        </mat-expansion-panel-header>
        <div class="grid-row-vitals">
            <mat-form-field appearance="outline"><mat-label>BP (Sys/Dia)</mat-label><input matInput formControlName="bloodPressure" placeholder="120/80"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Heart Rate (bpm)</mat-label><input matInput type="number" formControlName="heartRate"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Temp (°C)</mat-label><input matInput type="number" step="0.1" formControlName="temperature"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>SpO2 (%)</mat-label><input matInput type="number" formControlName="spo2"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Respiration (rpm)</mat-label><input matInput type="number" formControlName="respiration"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Weight (kg)</mat-label><input matInput type="number" formControlName="weight"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Height (cm)</mat-label><input matInput type="number" formControlName="height"></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Pain Scale (1-10)</mat-label><mat-select formControlName="painScale"><mat-option *ngFor="let i of [0,1,2,3,4,5,6,7,8,9,10]" [value]="i">{{i}}</mat-option></mat-select></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Conscious Level</mat-label><mat-select formControlName="consciousness"><mat-option value="ALERT">Alert</mat-option><mat-option value="VERBAL">Verbal Response</mat-option><mat-option value="PAIN">Pain Response</mat-option><mat-option value="UNRESPONSIVE">Unresponsive</mat-option></mat-select></mat-form-field>
        </div>
      </mat-expansion-panel>

      <mat-expansion-panel>
        <mat-expansion-panel-header><mat-panel-title>Clinical Presentation</mat-panel-title></mat-expansion-panel-header>
        <div class="full-width-form">
          <mat-form-field appearance="outline" class="w-100">
            <mat-label>Primary Symptoms & Complaints</mat-label>
            <textarea matInput formControlName="symptoms" rows="3" placeholder="Chief complaints, duration, severity..."></textarea>
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-100">
            <mat-label>Initial Diagnosis / Assessment</mat-label>
            <textarea matInput formControlName="assessment" rows="3" placeholder="Clinical impression..."></textarea>
          </mat-form-field>
        </div>
      </mat-expansion-panel>

      <mat-expansion-panel>
        <mat-expansion-panel-header><mat-panel-title>Social & Family History</mat-panel-title></mat-expansion-panel-header>
        <div class="grid-row-vitals">
          <mat-form-field appearance="outline"><mat-label>Smoking Status</mat-label><mat-select formControlName="smoking"><mat-option value="NEVER">Never</mat-option><mat-option value="FORMER">Former</mat-option><mat-option value="CURRENT">Current</mat-option></mat-select></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Alcohol Intensity</mat-label><mat-select formControlName="alcohol"><mat-option value="NONE">None</mat-option><mat-option value="OCCASIONAL">Occasional</mat-option><mat-option value="FREQUENT">Frequent</mat-option></mat-select></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Disability Status</mat-label><mat-select formControlName="disability"><mat-option value="NONE">None</mat-option><mat-option value="PHYSICAL">Physical</mat-option><mat-option value="SENSORY">Sensory</mat-option><mat-option value="COGNITIVE">Cognitive</mat-option></mat-select></mat-form-field>
        </div>
        <mat-form-field appearance="outline" class="w-100">
          <mat-label>Known Family Conditions</mat-label>
          <input matInput formControlName="familyHistory" placeholder="Diabetes, Hypertension, Cardiac..."/>
        </mat-form-field>
      </mat-expansion-panel>

      <mat-expansion-panel>
        <mat-expansion-panel-header>
          <mat-panel-title>Allergies & Active Medications</mat-panel-title>
        </mat-expansion-panel-header>
        <div class="split-arrays">
          <div formArrayName="allergies" class="array-col">
            <h4>Allergies</h4>
             <div *ngFor="let a of allergies.controls; let i=index" class="array-item">
                <mat-form-field appearance="outline" class="flex-grow">
                  <input matInput [formControlName]="i" placeholder="e.g. Penicillin">
                </mat-form-field>
                <button mat-icon-button color="warn" (click)="removeArrayItem('allergies', i)"><mat-icon>delete</mat-icon></button>
             </div>
             <button mat-stroked-button color="primary" size="small" (click)="addArrayItem('allergies')"><mat-icon>add</mat-icon> Allergy</button>
          </div>
          <div formArrayName="medications" class="array-col">
            <h4>Medications</h4>
             <div *ngFor="let m of medications.controls; let i=index" class="array-item">
                <mat-form-field appearance="outline" class="flex-grow">
                  <input matInput [formControlName]="i" placeholder="e.g. Metformin 500mg">
                </mat-form-field>
                <button mat-icon-button color="warn" (click)="removeArrayItem('medications', i)"><mat-icon>delete</mat-icon></button>
             </div>
             <button mat-stroked-button color="primary" size="small" (click)="addArrayItem('medications')"><mat-icon>add</mat-icon> Med</button>
          </div>
        </div>
      </mat-expansion-panel>
    </div>
  `,
  styles: [`
    .medical-history { display: flex; flex-direction: column; gap: 12px; margin-top: 16px; }
    .grid-row-vitals { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
    .array-item { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
    .flex-grow { flex: 1; }
    .w-100 { width: 100%; }
    .split-arrays { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
    h4 { font-size: 13px; font-weight: 700; color: #94a3b8; margin-bottom: 8px; text-transform: uppercase; }
    mat-expansion-panel { background: rgba(15, 23, 42, 0.4) !important; color: #fff; border: 1px solid rgba(255, 255, 255, 0.05); }
    mat-panel-title { color: #818cf8; font-weight: 600; font-size: 14px; }
  `]

})
export class MedicalHistoryComponent implements OnInit {
  @Input() form!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    if (!this.form.contains('bloodPressure')) {
      this.form.addControl('bloodPressure', this.fb.control(''));
      this.form.addControl('heartRate', this.fb.control(''));
      this.form.addControl('temperature', this.fb.control(''));
      this.form.addControl('spo2', this.fb.control(''));
      this.form.addControl('respiration', this.fb.control(''));
      this.form.addControl('weight', this.fb.control(''));
      this.form.addControl('height', this.fb.control(''));
      this.form.addControl('painScale', this.fb.control(0));
      this.form.addControl('consciousness', this.fb.control('ALERT'));
      this.form.addControl('symptoms', this.fb.control(''));
      this.form.addControl('assessment', this.fb.control(''));
      this.form.addControl('smoking', this.fb.control('NEVER'));
      this.form.addControl('alcohol', this.fb.control('NONE'));
      this.form.addControl('disability', this.fb.control('NONE'));
      this.form.addControl('familyHistory', this.fb.control(''));
      this.form.addControl('allergies', this.fb.array([]));
      this.form.addControl('medications', this.fb.array([]));
    }
  }

  get allergies() { return this.form.get('allergies') as FormArray; }
  get medications() { return this.form.get('medications') as FormArray; }

  addArrayItem(arrayName: string) {
    const arr = this.form.get(arrayName) as FormArray;
    arr.push(this.fb.control(''));
  }

  removeArrayItem(arrayName: string, index: number) {
    const arr = this.form.get(arrayName) as FormArray;
    arr.removeAt(index);
  }
}
