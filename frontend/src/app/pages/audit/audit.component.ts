import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';

import { ApiService, AuditLogDto } from '../../shared/api.service';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './audit.component.html',
  styleUrl: './audit.component.scss',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(15px)' }),
        animate('500ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ])
  ]
})
export class AuditComponent implements OnInit {
  loading = true;
  logs: AuditLogDto[] = [];
  displayedColumns = ['occurredAt', 'userId', 'action', 'tenantId'];

  constructor(private readonly api: ApiService) {}

  ngOnInit() {
    this.api.auditLog().subscribe({
      next: (logs) => {
        this.logs = logs.sort((a, b) => (new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime()));
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}