import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { animate, style, transition, trigger } from '@angular/animations';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService, BedDto } from '../../shared/api.service';
import { MedicalHistoryComponent } from '../patients/medical-history.component';
import { validAge, indianPhoneNumber, pinCode, bloodGroupValidator } from './medical.validators';

@Component({
  selector: 'app-admission',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MedicalHistoryComponent
  ],
  templateUrl: './admission.component.html',
  styleUrl: './admission.component.scss',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(15px)' }),
        animate('500ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ])
  ]
})
export class AdmissionComponent implements OnInit {
  beds: BedDto[] = [];
  loading = true;
  submitting = false;

  demographicsForm: FormGroup;
  clinicalForm: FormGroup;
  insuranceForm: FormGroup;
  logisticsForm: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: ApiService,
    private readonly snackBar: MatSnackBar
  ) {
    this.demographicsForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      dob: ['', [Validators.required, validAge(0, 120)]],
      gender: ['MALE', [Validators.required]],
      ssn: [''],
      nationality: [''],
      maritalStatus: ['SINGLE'],
      occupation: [''],
      language: ['English'],
      religion: [''],
      bloodType: ['', [bloodGroupValidator]],
      weight: [''],
      height: [''],
      email: ['', [Validators.email]],
      phone: ['', [Validators.required, indianPhoneNumber]],
      address: [''],
      city: [''],
      state: [''],
      zip: ['', [pinCode]],
      country: ['USA'],
      emerName: ['', [Validators.required]],
      emerRelation: ['', [Validators.required]],
      emerPhone: ['', [Validators.required]],
      emerEmail: [''],
      emerAddress: [''],
      employerName: [''],
      employerPhone: [''],
      employerAddress: [''],
      primaryDoctor: [''],
      referringDoctor: [''],
      tobaccoUse: ['NO'],
      alcoholUse: ['NO'],
      drugUse: ['NO'],
      exerciseFrequency: [''],
      dietaryPreference: [''],
      organDonor: [false],
      advancedDirective: [false],
      preferredPharmacy: [''],
      pharmacyPhone: [''],
      intakeTemp: [''],
      intakePulse: [''],
      intakeBP: [''],
      intakeSpO2: [''],
      intakeRR: [''],
      currentMedications: [''],
      knownAllergies: [''],
      pastSurgeries: [''],
      familyHistory: [''],
      reasonForAdmission: ['', Validators.required]
    });

    this.clinicalForm = this.fb.group({
      history: this.fb.group({})
    });

    this.insuranceForm = this.fb.group({
      provider: ['', [Validators.required]],
      policyNumber: ['', [Validators.required]],
      groupNumber: [''],
      expiry: [''],
      secondaryProvider: ['']
    });

    this.logisticsForm = this.fb.group({
      bedId: ['', [Validators.required]],
      notes: ['']
    });
  }

  get clinicalHistoryForm(): FormGroup {
    return this.clinicalForm.get('history') as FormGroup;
  }

  ngOnInit() {
    this.api.listBeds().subscribe({
      next: (beds) => {
        this.beds = beds.filter(b => b.status === 'AVAILABLE');
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  submit() {
    if (this.demographicsForm.invalid || this.logisticsForm.invalid || this.insuranceForm.invalid) return;

    this.submitting = true;
    const allDemographics = this.demographicsForm.value;
    const { firstName, lastName, dob, gender, email, phone, address, ...extraData } = allDemographics;
    const insurance = this.insuranceForm.value;
    const logistics = this.logisticsForm.value;

    this.api.admitPatient({
      patient: {
        firstName,
        lastName,
        dob,
        gender,
        email,
        phone,
        address,
        medicalHistory: JSON.stringify({ 
          ...extraData, 
          insurance, 
          admittingNotes: logistics.notes,
          timestamp: new Date().toISOString()
        })
      },
      bedId: logistics.bedId!,
      notes: logistics.notes || 'Routine clinical intake'
    }).subscribe({
      next: () => {
        this.snackBar.open('Patient high-priority admission completed.', 'Close', { duration: 5000 });
        this.submitting = false;
        this.demographicsForm.reset({ gender: 'MALE', maritalStatus: 'SINGLE', language: 'English' });
        this.clinicalForm.reset();
        this.insuranceForm.reset();
        this.logisticsForm.reset();
      },
      error: (err) => {
        this.snackBar.open(err?.error?.message || 'Admission synchronization failed.', 'Close', { duration: 4000 });
        this.submitting = false;
      }
    });
  }
}