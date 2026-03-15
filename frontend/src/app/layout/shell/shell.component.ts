import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { map, shareReplay } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { AuthService } from '../../auth/auth.service';

type NavItem = {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
};

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  isSidebarCollapsed = false;

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  constructor(
    public readonly auth: AuthService,
    private breakpointObserver: BreakpointObserver
  ) {
    this.isHandset$.subscribe(isHandset => {
      if (isHandset) this.isSidebarCollapsed = true;
    });
  }

  private _cachedNav: NavItem[] | null = null;

  get nav(): NavItem[] {
    if (this._cachedNav) return this._cachedNav;

    const roles = this.auth.roles();
    
    if (roles.includes('ROLE_ADMIN')) {
      this._cachedNav = [
        { label: 'Dashboard', icon: 'space_dashboard', route: '/admin/dashboard' },
        { label: 'Patients', icon: 'person', route: '/admin/patients' },
        { label: 'Appointments', icon: 'event', route: '/admin/appointments' },
        { label: 'Analytics', icon: 'analytics', route: '/admin/analytics' },
        { label: 'Inventory', icon: 'inventory_2', route: '/admin/inventory' },
      ];
    } else if (roles.includes('ROLE_DOCTOR')) {
      this._cachedNav = [
        { label: 'Dashboard', icon: 'space_dashboard', route: '/doctor/dashboard' },
        { label: 'Patients', icon: 'person', route: '/doctor/patients' },
        { label: 'Appointments', icon: 'event', route: '/doctor/appointments' },
      ];
    } else if (roles.includes('ROLE_NURSE')) {
      this._cachedNav = [
        { label: 'Dashboard', icon: 'space_dashboard', route: '/nurse/dashboard' },
        { label: 'Patients', icon: 'person', route: '/nurse/patients' },
        { label: 'Beds', icon: 'hotel', route: '/nurse/beds' },
        { label: 'Admissions', icon: 'assignment', route: '/nurse/admissions' },
      ];
    } else {
      this._cachedNav = [];
    }
    
    return this._cachedNav;
  }

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }
}