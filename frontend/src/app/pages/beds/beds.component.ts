import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiService, BedDto } from '../../shared/api.service';

@Component({
  selector: 'app-beds',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './beds.component.html',
  styleUrl: './beds.component.scss',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(15px)' }),
        animate('500ms cubic-bezier(0.4, 0, 0.2, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ])
  ]
})
export class BedsComponent implements OnInit {
  loading = true;
  beds: BedDto[] = [];
  displayedColumns = ['ward', 'room', 'bedNumber', 'status', 'actions'];

  statuses = ['AVAILABLE', 'OCCUPIED', 'CLEANING', 'MAINTENANCE'];

  constructor(
    private readonly api: ApiService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading = true;
    this.api.listBeds().subscribe({
      next: (beds) => {
        this.beds = beds;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('System error: Failed to retrieve ward inventory.', 'Close', { duration: 4000 });
      }
    });
  }

  setStatus(b: BedDto, status: string) {
    this.api.updateBedStatus(b.id, status).subscribe({
      next: (updated) => {
        this.beds = this.beds.map((x) => (x.id === updated.id ? updated : x));
        this.snackBar.open(`Asset ID ${updated.bedNumber} status updated to ${updated.status}.`, 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Authorization error: Bed status override rejected.', 'Close', { duration: 4000 });
      }
    });
  }
}