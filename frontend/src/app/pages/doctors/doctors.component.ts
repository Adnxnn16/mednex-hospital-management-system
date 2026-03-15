import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { animate, style, transition, trigger } from '@angular/animations';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService, DoctorDto } from '../../shared/api.service';

@Component({
  selector: 'app-doctors',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './doctors.component.html',
  styleUrl: './doctors.component.scss',
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
export class DoctorsComponent implements OnInit {
  loading = true;
  submitting = false;
  showForm = false;
  doctors: DoctorDto[] = [];
  displayedColumns = ['fullName', 'email', 'specialty', 'createdAt'];
  form: FormGroup;

  constructor(
    private readonly api: ApiService,
    private readonly fb: FormBuilder,
    private readonly snack: MatSnackBar
  ) {
    this.form = this.fb.group({
      fullName: ['', [Validators.required]],
      email: ['', [Validators.email]],
      specialty: ['']
    });
  }

  ngOnInit() {
    this.load();
  }

  toggleForm() {
    this.showForm = !this.showForm;
  }

  load() {
    this.loading = true;
    this.api.listDoctors().subscribe({
      next: (d) => {
        this.doctors = d;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  submit() {
    if (this.form.invalid) return;

    this.submitting = true;
    this.api.createDoctor(this.form.value).subscribe({
      next: () => {
        this.submitting = false;
        this.showForm = false;
        this.form.reset();
        this.snack.open('Doctor registered successfully', 'Close', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.submitting = false;
        this.snack.open('Error registering staff', 'Close', { duration: 3000 });
      }
    });
  }
}
