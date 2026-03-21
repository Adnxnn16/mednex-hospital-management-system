import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { animate, query, stagger, style, transition, trigger } from '@angular/animations';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { AuthService } from '../../auth/auth.service';

type Role = {
  key: 'admin' | 'doctor' | 'nurse';
  title: string;
  icon: string;
  subtitle: string;
  desc: string;
};

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        query(
          '.stagger',
          [
            style({ opacity: 0, transform: 'translateY(20px)' }),
            stagger(80, [animate('500ms cubic-bezier(0.35, 0, 0.25, 1)', style({ opacity: 1, transform: 'translateY(0)' }))])
          ],
          { optional: true }
        )
      ])
    ])
  ]
})
export class LandingComponent {
  activeRole: Role['key'] = 'doctor';
  showPassword = false;
  roles: Role[] = [
    {
      key: 'admin',
      title: 'Admin',
      icon: 'admin_panel_settings',
      subtitle: '',
      desc: ''
    },
    {
      key: 'doctor',
      title: 'Doctor',
      icon: 'medical_services',
      subtitle: '',
      desc: ''
    },
    {
      key: 'nurse',
      title: 'Nurse',
      icon: 'healing',
      subtitle: '',
      desc: ''
    }
  ];

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {
    if (this.auth.isLoggedIn()) {
      const roles = this.auth.roles();
      if (roles.includes('ROLE_ADMIN')) this.router.navigateByUrl('/admin/dashboard');
      else if (roles.includes('ROLE_DOCTOR')) this.router.navigateByUrl('/doctor/dashboard');
      else if (roles.includes('ROLE_NURSE')) this.router.navigateByUrl('/nurse/dashboard');
      else this.router.navigateByUrl('/login');
    } else {
        this.router.navigateByUrl('/login');
    }
  }

  login(role: Role['key']) {
    this.router.navigate(['/login', role]);
  }


  signup(role: Role['key']) {
    this.auth.signup(role);
  }
}