import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';

import { ApiService, AppointmentDto, PatientDto } from '../../../shared/api.service';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FullCalendarModule],
  template: `
<div class="doctor-dash" @.disabled>

  <!-- Welcome Banner -->
  <div class="welcome-banner">
    <div class="welcome-text">
      <h2>Good {{ greeting }}, {{ auth.username() }}</h2>
      <p>Here's your clinical overview for today</p>
    </div>
    <div class="quick-stats">
      <div class="qstat">
        <span class="qstat-val">{{ todayCount }}</span>
        <span class="qstat-lbl">Today's Appts</span>
      </div>
      <div class="qstat">
        <span class="qstat-val">{{ patientCount }}</span>
        <span class="qstat-lbl">Total Patients</span>
      </div>
      <div class="qstat">
        <span class="qstat-val">{{ pendingCount }}</span>
        <span class="qstat-lbl">Pending Review</span>
      </div>
    </div>
  </div>

  <div class="dash-grid">
    <!-- Calendar -->
    <div class="dash-card calendar-card">
      <div class="card-head">
        <span class="material-symbols-outlined">calendar_month</span>
        <h3>Appointment Schedule</h3>
      </div>
      <full-calendar [options]="calendarOptions"></full-calendar>
    </div>

    <!-- Today's Appointments List -->
    <div class="dash-card">
      <div class="card-head">
        <span class="material-symbols-outlined">event_available</span>
        <h3>Today's Appointments</h3>
        <a [routerLink]="['/doctor/appointments']" class="view-all">View All</a>
      </div>
      <div *ngIf="todayAppointments.length === 0" class="empty-state">
        <span class="material-symbols-outlined">celebration</span>
        <p>No appointments scheduled for today</p>
      </div>
      <div class="appt-list">
        <div *ngFor="let appt of todayAppointments" class="appt-item">
          <div class="appt-time">{{ appt.startTime | date:'HH:mm' }}</div>
          <div class="appt-detail">
            <span class="appt-title">Consultation</span>
            <span class="appt-status" [class]="'status-' + appt.status.toLowerCase()">{{ appt.status }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Patients -->
    <div class="dash-card">
      <div class="card-head">
        <span class="material-symbols-outlined">person_search</span>
        <h3>Recent Patients</h3>
        <a [routerLink]="['/doctor/patients']" class="view-all">View All</a>
      </div>
      <div *ngIf="loading" class="loading-row">
        <div class="shimmer"></div><div class="shimmer"></div><div class="shimmer"></div>
      </div>
      <div class="patient-list" *ngIf="!loading">
        <div *ngFor="let p of patients" class="patient-row">
          <div class="patient-avatar">{{ p.firstName[0] }}{{ p.lastName[0] }}</div>
          <div class="patient-info">
            <span class="patient-name">{{ p.firstName }} {{ p.lastName }}</span>
            <span class="patient-sub">{{ p.gender ?? 'Unknown' }} · {{ p.dob | date:'MMM d, y' }}</span>
          </div>
          <span class="badge-chip">Active</span>
        </div>
      </div>
    </div>
  </div>
</div>
  `,
  styles: [`
    .doctor-dash { padding: 28px; }

    .welcome-banner {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: linear-gradient(135deg, rgba(34,163,255,0.12), rgba(99,102,241,0.08));
      border: 1px solid rgba(34,163,255,0.2);
      border-radius: 20px;
      padding: 28px 32px;
      margin-bottom: 28px;
    }
    .welcome-text h2 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: white; }
    .welcome-text p  { margin: 0; color: #64748B; font-size: 14px; }

    .quick-stats { display: flex; gap: 32px; }
    .qstat { text-align: center; }
    .qstat-val { display: block; font-size: 28px; font-weight: 800; color: #22A3FF; }
    .qstat-lbl { font-size: 12px; color: #64748B; font-weight: 500; }

    .dash-grid {
      display: grid;
      grid-template-columns: 1fr 320px;
      grid-template-rows: auto auto;
      gap: 20px;
    }
    .calendar-card { grid-row: span 2; }

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
      .material-symbols-outlined { color: #22A3FF; font-size: 20px; }
      h3 { margin: 0; font-size: 15px; font-weight: 600; color: white; flex: 1; }
      .view-all { font-size: 12px; color: #22A3FF; text-decoration: none; font-weight: 600; }
    }

    /* Appointment list */
    .appt-list { display: flex; flex-direction: column; gap: 8px; }
    .appt-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; background: rgba(255,255,255,0.03); border-radius: 10px; }
    .appt-time { font-size: 13px; font-weight: 600; color: #22A3FF; min-width: 44px; }
    .appt-detail { display: flex; flex-direction: column; gap: 2px; flex: 1; }
    .appt-title { font-size: 13px; color: white; font-weight: 500; }
    .appt-status { font-size: 11px; font-weight: 600; }
    .status-booked    { color: #f59e0b; }
    .status-scheduled { color: #38bdf8; }
    .status-completed { color: #22c55e; }
    .status-cancelled { color: #f87171; }

    /* Patients */
    .patient-list { display: flex; flex-direction: column; gap: 6px; }
    .patient-row { display: flex; align-items: center; gap: 12px; padding: 8px; border-radius: 10px; transition: background 0.15s; }
    .patient-row:hover { background: rgba(255,255,255,0.03); }
    .patient-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg,#22A3FF,#6366f1); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; color: white; flex-shrink: 0; }
    .patient-info { flex: 1; }
    .patient-name { display: block; font-size: 13px; font-weight: 600; color: white; }
    .patient-sub  { font-size: 11px; color: #64748B; }
    .badge-chip { font-size: 10px; font-weight: 700; padding: 3px 8px; background: rgba(34,197,94,0.12); color: #22c55e; border-radius: 8px; }

    /* Loading shimmer */
    .loading-row { display: flex; flex-direction: column; gap: 8px; }
    .shimmer { height: 48px; border-radius: 10px; background: linear-gradient(90deg,rgba(255,255,255,0.04) 25%, rgba(255,255,255,0.08) 50%, rgba(255,255,255,0.04) 75%); background-size: 200% 100%; animation: shimmer 1.2s infinite; }
    @keyframes shimmer { 0%{background-position:200% 0} 100%{background-position:-200% 0} }

    /* Empty state */
    .empty-state { text-align: center; padding: 24px; color: #475569; span { font-size: 32px; display: block; margin-bottom: 8px; } p { font-size: 13px; margin: 0; } }

    /* FullCalendar overrides */
    ::ng-deep .fc { color: #e2e8f0; font-size: 13px; }
    ::ng-deep .fc .fc-toolbar-title { font-size: 15px; font-weight: 700; }
    ::ng-deep .fc-theme-standard td, ::ng-deep .fc-theme-standard th { border-color: rgba(255,255,255,0.06); }
    ::ng-deep .fc .fc-col-header-cell-cushion { color: #64748B; }
    ::ng-deep .fc .fc-daygrid-day-number { color: #94A3B8; }
    ::ng-deep .fc .fc-button { background: rgba(255,255,255,0.05); border-color: rgba(255,255,255,0.1); color: #e2e8f0; font-size: 12px; }
    ::ng-deep .fc .fc-button-active { background: #22A3FF !important; border-color: #22A3FF !important; }
  `]
})
export class DoctorDashboardComponent implements OnInit {
  loading = true;
  greeting = 'Morning';
  patients: PatientDto[] = [];
  todayAppointments: AppointmentDto[] = [];
  patientCount = 0;
  todayCount = 0;
  pendingCount = 0;

  calendarOptions: CalendarOptions = {
    initialView: 'timeGridWeek',
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    slotMinTime: '08:00:00',
    slotMaxTime: '18:00:00',
    height: 420,
    events: []
  };

  constructor(private api: ApiService, public auth: AuthService) {}

  ngOnInit(): void {
    const h = new Date().getHours();
    this.greeting = h < 12 ? 'Morning' : h < 17 ? 'Afternoon' : 'Evening';

    this.api.listPatients().subscribe(p => {
      this.patients = p.slice(0, 6);
      this.patientCount = p.length;
      this.loading = false;
    });

    this.api.listAppointments().subscribe(a => {
      const today = new Date().toISOString().split('T')[0];
      this.todayAppointments = a.filter(apt => apt.startTime.startsWith(today));
      this.todayCount = this.todayAppointments.length;
      this.pendingCount = a.filter(apt => apt.status === 'BOOKED' || apt.status === 'SCHEDULED').length;
      this.calendarOptions = {
        ...this.calendarOptions,
        events: a.map(apt => ({
          title: 'Consultation',
          start: apt.startTime,
          end: apt.endTime,
          backgroundColor: apt.status === 'COMPLETED' ? '#22c55e' : apt.status === 'CANCELLED' ? '#f87171' : '#22A3FF',
          borderColor: 'transparent'
        }))
      };
    });
  }
}
