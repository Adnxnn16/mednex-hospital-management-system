import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, BedOccupancyDto } from '../../../shared/api.service';
import { NgChartsModule } from 'ng2-charts';
import type { ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  template: `
    <div class="analytics-container animate-up">
      <div class="header">
        <h1>Advanced Analytics</h1>
        <p>Real-time data insights from MedNex systems</p>
      </div>

      <div class="analytics-grid">
        <div class="chart-card">
          <h3>Bed Occupancy Overview</h3>
          <div class="chart-container">
            <canvas baseChart
              [data]="bedData"
              [type]="'doughnut'"
              [options]="chartOptions">
            </canvas>
          </div>
          <div class="stats-row" *ngIf="occupancy">
            <div class="stat">
              <span class="label">Total Beds</span>
              <span class="value">{{ occupancy.totalBeds }}</span>
            </div>
            <div class="stat">
              <span class="label">Occupied</span>
              <span class="value">{{ occupancy.occupiedBeds }}</span>
            </div>
            <div class="stat">
              <span class="label">Rate</span>
              <span class="value">{{ occupancy.occupancyRate }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .analytics-container { padding: 32px; }
    .header { margin-bottom: 32px; }
    h1 { font-size: 28px; font-weight: 700; margin: 0 0 8px; }
    p { color: #94A3B8; }
    .analytics-grid { display: grid; grid-template-columns: 1fr; gap: 24px; }
    .chart-card { background: #111827; border: 1px solid rgba(255,255,255,0.05); border-radius: 20px; padding: 24px; }
    .chart-container { height: 300px; position: relative; }
    .stats-row { display: flex; gap: 40px; margin-top: 24px; padding-top: 24px; border-top: 1px solid rgba(255,255,255,0.05); }
    .stat { display: flex; flex-direction: column; gap: 4px; }
    .label { font-size: 12px; color: #64748B; text-transform: uppercase; font-weight: 700; }
    .value { font-size: 20px; font-weight: 700; color: #22A3FF; }
  `]
})
export class AnalyticsComponent implements OnInit {
  occupancy?: BedOccupancyDto;
  bedData: ChartData<'doughnut'> = {
    labels: ['Occupied', 'Available'],
    datasets: [{ data: [0, 100], backgroundColor: ['#22A3FF', '#1E293B'], borderWidth: 0 }]
  };
  chartOptions = { responsive: true, cutout: '70%', plugins: { legend: { display: false } } };

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.api.bedOccupancy().subscribe(data => {
      this.occupancy = data;
      this.bedData.datasets[0].data = [data.occupiedBeds, data.totalBeds - data.occupiedBeds];
    });
  }
}
