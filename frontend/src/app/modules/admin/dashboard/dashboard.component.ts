import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AdminDashboardService } from './dashboard.service';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    NgChartsModule
  ],

  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  stats: any;
  quickInsights: any;
  activeAdmissions: any[] = [];
  displayedColumns: string[] = ['patient', 'department', 'status', 'admitted', 'action'];

  // Chart Properties
  public doughnutChartOptions: any = {
    responsive: true,
    cutout: '70%',
    plugins: {
      legend: { display: false }
    }
  };
  
  public doughnutChartLabels: string[] = ['ICU', 'General Ward', 'Pediatrics'];
  public doughnutChartData: ChartData<'doughnut'> = {
    labels: this.doughnutChartLabels,
    datasets: [
      { 
        data: [94, 68, 45],
        backgroundColor: ['#22A3FF', '#22C55E', '#F59E0B'],
        borderWidth: 0,
        hoverOffset: 4
      }
    ]
  };
  public doughnutChartType: ChartType = 'doughnut';

  constructor(private dashboardService: AdminDashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getStats().subscribe(s => this.stats = s);
    this.dashboardService.getQuickInsights().subscribe(qi => this.quickInsights = qi);
    this.dashboardService.getActiveAdmissions().subscribe(aa => this.activeAdmissions = aa);
    
    this.dashboardService.getBedOccupancy().subscribe(data => {
      this.doughnutChartData = {
        labels: data.map(d => d.label),
        datasets: [{
          data: data.map(d => d.value),
          backgroundColor: data.map(d => d.color),
          borderWidth: 0,
          hoverOffset: 4
        }]
      };
    });
  }

  getChartColor(index: number): string {
    const bg = this.doughnutChartData.datasets[0].backgroundColor;
    if (Array.isArray(bg)) {
      return bg[index] as string;
    }
    return '#ccc';
  }

  exportData() {
    let csv = 'Patient ID,Patient Name,Department,Status,Admitted Date\n';
    this.activeAdmissions.forEach(adm => {
      // Escape fields safely for CSV
      const id = `"${adm.patientId || ''}"`;
      const name = `"${adm.patient || ''}"`;
      const dept = `"${adm.department || ''}"`;
      const status = `"${adm.status || ''}"`;
      const admitted = `"${adm.admitted || ''}"`;
      csv += `${id},${name},${dept},${status},${admitted}\n`;
    });

    // Create a Blob and trigger a download
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `hospital_report_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}

