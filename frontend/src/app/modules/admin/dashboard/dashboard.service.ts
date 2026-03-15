import { Injectable } from '@angular/core';
import { ApiService } from '../../../shared/api.service';
import { map, forkJoin, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminDashboardService {
  constructor(private api: ApiService) {}

  getStats() {
    return forkJoin({
      patients: this.api.listPatients(),
      beds: this.api.listBeds(),
      appointments: this.api.listAppointments()
    }).pipe(
      map(data => ({
        totalPatients: data.patients.length,
        patientTrend: 12.5,
        bedsAvailable: data.beds.filter(b => b.status === 'AVAILABLE').length,
        appointmentsToday: data.appointments.length,
        appointmentTrend: 3.2,
        activeSchedule: data.appointments.filter(a => a.status === 'BOOKED').length,
        scheduleTrend: -1.4
      }))
    );
  }

  getBedOccupancy() {
    return this.api.bedOccupancy().pipe(
      map(data => [
        { label: 'Occupied', value: data.occupiedBeds, color: '#22A3FF' },
        { label: 'Available', value: data.totalBeds - data.occupiedBeds, color: '#22C55E' }
      ])
    );
  }

  getQuickInsights() {
    return of({
      erWaitTime: '42m (High)',
      labProcessing: 'Optimal',
      staffEfficiency: '8.4/10'
    });
  }

  getActiveAdmissions() {
    return this.api.listAdmissions().pipe(
      map(admissions => admissions.slice(0, 5).map(adm => ({
        id: adm.id.substring(0, 2).toUpperCase(),
        patient: 'Patient #' + adm.patientId.substring(0, 4),
        patientId: '#PX-' + adm.patientId.substring(0, 4),
        department: 'General',
        status: 'Stable',
        admitted: new Date(adm.admittedAt).toLocaleDateString()
      })))
    );
  }
}
