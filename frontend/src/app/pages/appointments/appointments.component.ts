import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { animate, style, transition, trigger } from '@angular/animations';
import { MatSnackBar } from '@angular/material/snack-bar';

import { FullCalendarModule } from '@fullcalendar/angular';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';

import { ApiService, AppointmentDto, DoctorDto, PatientDto } from '../../shared/api.service';

@Component({
  selector: 'app-appointments',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FullCalendarModule
  ],
  templateUrl: './appointments.component.html',
  styleUrl: './appointments.component.scss',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(15px)' }),
        animate('500ms cubic-bezier(0.35, 0, 0.25, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ]),
    trigger('slideInOut', [
      transition(':enter', [
        style({ height: '0', opacity: 0, overflow: 'hidden', marginBottom: '0' }),
        animate('300ms cubic-bezier(0.4, 0, 0.2, 1)', style({ height: '*', opacity: 1, marginBottom: '32px' }))
      ]),
      transition(':leave', [
        animate('250ms cubic-bezier(0.4, 0, 1, 1)', style({ height: '0', opacity: 0, marginBottom: '0' }))
      ])
    ])
  ]
})
export class AppointmentsComponent implements OnInit {
  appointments: AppointmentDto[] = [];
  patients: PatientDto[] = [];
  doctors: DoctorDto[] = [];
  loading = true;
  showForm = false;
  submitting = false;

  form = this.fb.group({
    patientId: ['', [Validators.required]],
    doctorId: ['', [Validators.required]],
    startTime: ['', [Validators.required]],
    endTime: ['', [Validators.required]]
  });

  calendarOptions: any = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek'
    },
    height: 'auto',
    selectable: true,
    nowIndicator: true,
    dateClick: (info: any) => this.onDateClick(info),
    events: [] as any[]
  };

  constructor(
    private readonly api: ApiService,
    private readonly fb: FormBuilder,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.loading = true;
    this.api.listAppointments().subscribe({
      next: (appts) => {
        this.appointments = appts;
        this.calendarOptions = {
          ...this.calendarOptions,
          events: appts.map((a) => ({
            id: a.id,
            title: `Appt: ${a.status}`,
            start: a.startTime,
            end: a.endTime,
            backgroundColor: a.status === 'BOOKED' ? '#3b82f6' : '#10b981',
            borderColor: 'transparent',
            textColor: '#fff'
          }))
        };
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });

    this.api.listPatients().subscribe({ next: (p) => (this.patients = p) });
    this.api.listDoctors().subscribe({ next: (d) => (this.doctors = d) });
  }

  onDateClick(info: any) {
    const dateStr = info.dateStr;
    const startTime = dateStr.includes('T') ? dateStr : dateStr + 'T09:00:00';
    const endDate = new Date(startTime);
    endDate.setHours(endDate.getHours() + 1);
    const endTime = endDate.toISOString().slice(0, 19);

    this.form.patchValue({ startTime, endTime });
    this.showForm = true;
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (this.showForm) {
      this.form.reset();
    }
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const v = this.form.getRawValue();

    this.api
      .createAppointment({
        patientId: v.patientId!,
        doctorId: v.doctorId!,
        startTime: v.startTime! + (v.startTime!.includes('+') || v.startTime!.includes('Z') ? '' : '+00:00'),
        endTime: v.endTime! + (v.endTime!.includes('+') || v.endTime!.includes('Z') ? '' : '+00:00')
      })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.showForm = false;
          this.snackBar.open('Appointment synchronized successfully', 'Close', { duration: 3000 });
          this.loadAll();
        },
        error: (err) => {
          this.submitting = false;
          this.snackBar.open(err?.error?.message || 'Sync failed. Resource conflict detected.', 'Close', { duration: 4000 });
        }
      });
  }
}