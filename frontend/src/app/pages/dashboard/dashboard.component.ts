import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { animate, query, stagger, style, transition, trigger } from '@angular/animations';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartType, ChartOptions } from 'chart.js';

// Calendar Imports
import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';

import { ApiService, BedOccupancyDto, PatientDto, AppointmentDto, DoctorDto, AuditLogDto } from '../../shared/api.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    MatCardModule, 
    MatIconModule, 
    MatProgressSpinnerModule, 
    MatButtonModule,
    MatTooltipModule,
    NgChartsModule,
    FullCalendarModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        query('.stagger', [
          style({ opacity: 0, transform: 'translateY(15px)' }),
          stagger(60, [
            animate('500ms cubic-bezier(0.35, 0, 0.25, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
          ])
        ], { optional: true })
      ])
    ])
  ]
})
export class DashboardComponent implements OnInit {
  loading = true;
  userRole: 'admin' | 'doctor' | 'nurse' | 'user' = 'user';
  
  // Shared Metrics
  patientCount = 0;
  bedCount = 0;
  appointmentCount = 0;
  todayAppointments: AppointmentDto[] = [];
  occupancyRate = 0;
  
  // Admin Metrics
  doctorCount = 0;
  recentAuditLogs: AuditLogDto[] = [];
  admissionsTrend: number[] = [12, 19, 3, 5, 2, 3, 7]; // Simulated trend
  
  // Doctor Metrics
  myPatients: PatientDto[] = [];
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
    height: 'auto',
    events: []
  };
  
  // Nurse Metrics
  activeBeds: number = 0;
  cleaningBeds: number = 0;
  wardPatients: any[] = []; // Sub-list of patients for vitals recording

  // Chart Configs
  bedChartData: ChartConfiguration<'doughnut'>['data'] | null = null;
  trendChartData: ChartConfiguration<'line'>['data'] | null = null;
  
  chartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { boxWidth: 12, padding: 10, color: '#94a3b8' } }
    },
    cutout: '70%'
  };

  trendOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } },
      x: { grid: { display: false }, ticks: { color: '#94a3b8' } }
    },
    plugins: { legend: { display: false } }
  };

  constructor(
    private readonly api: ApiService,
    private readonly auth: AuthService
  ) {}

  ngOnInit() {
    const roles = this.auth.roles();
    if (roles.includes('ROLE_ADMIN'))  this.userRole = 'admin';
    else if (roles.includes('ROLE_DOCTOR')) this.userRole = 'doctor';
    else if (roles.includes('ROLE_NURSE'))  this.userRole = 'nurse';

    this.loadCommonData();
    if (this.userRole === 'admin')  this.loadAdminData();
    if (this.userRole === 'doctor') this.loadDoctorData();
    if (this.userRole === 'nurse')  this.loadNurseData();
  }

  private loadCommonData() {
    this.api.bedOccupancy().subscribe(o => {
      this.bedCount = o.totalBeds;
      this.occupancyRate = o.totalBeds > 0 ? Math.round((o.occupiedBeds / o.totalBeds) * 100) : 0;
      this.bedChartData = {
        labels: ['Occupied', 'Available'],
        datasets: [{
          data: [o.occupiedBeds, o.totalBeds - o.occupiedBeds],
          backgroundColor: ['#6366f1', 'rgba(255,255,255,0.05)'],
          borderWidth: 0
        }]
      };
      this.checkDone();
    });

    this.api.listPatients().subscribe(p => {
      this.patientCount = p.length;
      this.checkDone();
    });

    this.api.listAppointments().subscribe(a => {
      this.appointmentCount = a.length;
      const today = new Date().toISOString().split('T')[0];
      this.todayAppointments = a.filter(apt => apt.startTime.startsWith(today));
      
      // Update Doctor's Calendar
      this.calendarOptions.events = a.map(apt => ({
        title: `Consultation (${apt.status})`,
        start: apt.startTime,
        end: apt.endTime,
        backgroundColor: apt.status === 'CONFIRMED' ? '#10b981' : '#f59e0b',
        borderColor: 'transparent'
      }));

      this.checkDone();
    });
  }

  private loadAdminData() {
    this.api.listDoctors().subscribe(d => {
      this.doctorCount = d.length;
      this.checkDone();
    });
    this.api.auditLog().subscribe(logs => {
      this.recentAuditLogs = logs.slice(0, 5);
      this.checkDone();
    });
    this.trendChartData = {
      labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
      datasets: [{
        data: this.admissionsTrend,
        label: 'Admissions',
        borderColor: '#6366f1',
        tension: 0.4,
        fill: true,
        backgroundColor: 'rgba(99, 102, 241, 0.1)'
      }]
    };
  }

  private loadDoctorData() {
    this.api.listPatients().subscribe(p => {
      this.myPatients = p.slice(0, 3); // Simulated "assigned"
      this.checkDone();
    });
  }

  private loadNurseData() {
    this.api.listBeds().subscribe(b => {
      this.activeBeds = b.filter(bed => bed.status === 'OCCUPIED').length;
      this.cleaningBeds = b.filter(bed => bed.status === 'CLEANING').length;
      this.checkDone();
    });
    this.api.listPatients().subscribe(p => {
       // Just grab some active ones to monitor vitals
       this.wardPatients = p.slice(0, 5).map(pt => ({
         ...pt,
         latestBP: '120/80',
         latestHR: '72',
         latestTemp: '98.6',
         latestO2: '99',
         bedId: 'W1-B' + Math.floor(Math.random() * 20)
       }));
    });
  }

  private loaded = 0;
  private checkDone() {
    this.loaded++;
    if (this.loaded >= 3) this.loading = false;
  }
}