import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { animate, style, transition, trigger } from '@angular/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';

import { ApiService, PatientDto } from '../../shared/api.service';
import { MedicalHistoryComponent } from './medical-history.component';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MedicalHistoryComponent
  ],
  templateUrl: './patients.component.html',
  styleUrl: './patients.component.scss',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(15px)' }),
        animate('500ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ]),
    trigger('slideInOut', [
      transition(':enter', [
        style({ height: '0', opacity: 0, overflow: 'hidden' }),
        animate('300ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('250ms ease-in', style({ height: '0', opacity: 0 }))
      ])
    ])
  ]
})
export class PatientsComponent implements OnInit {
  loading = true;
  submitting = false;
  showForm = false;
  patients: PatientDto[] = [];
  displayedColumns = ['name', 'email', 'phone', 'gender', 'dob'];
  
  // High-fidelity multi-step form
  demographicsForm: FormGroup;
  clinicalForm: FormGroup;

  constructor(
    private readonly api: ApiService,
    private readonly fb: FormBuilder,
    private readonly snack: MatSnackBar
  ) {
    this.demographicsForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.email]],
      phone: [''],
      dob: [''],
      gender: ['MALE'],
      address: [''],
      bloodGroup: [''],
      occupation: [''],
      emergencyContactName: [''],
      emergencyContactPhone: [''],
      insuranceProvider: [''],
      policyNumber: ['']
    });

    this.clinicalForm = this.fb.group({
      history: this.fb.group({}) // Handled by MedicalHistoryComponent
    });
  }

  get clinicalHistoryForm(): FormGroup {
    return this.clinicalForm.get('history') as FormGroup;
  }

  ngOnInit() {
    this.load();
  }

  toggleForm() {
    this.showForm = !this.showForm;
  }

  load() {
    this.loading = true;
    this.api.listPatients().subscribe({
      next: (p) => {
        this.patients = p;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  submit() {
    if (this.demographicsForm.invalid) return;

    this.submitting = true;
    const history = this.clinicalForm.get('history')?.value;
    const payload = {
      ...this.demographicsForm.value,
      medicalHistory: JSON.stringify(history)
    };

    this.api.createPatient(payload).subscribe({
      next: () => {
        this.submitting = false;
        this.showForm = false;
        this.demographicsForm.reset({ gender: 'MALE' });
        this.clinicalForm.reset();
        this.snack.open('Patient record synchronized successfully', 'Close', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.submitting = false;
        this.snack.open('Error persisting clinical record', 'Close', { duration: 3000 });
      }
    });
  }
}