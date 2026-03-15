import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  templateUrl: './auth-callback.component.html'
})
export class AuthCallbackComponent implements OnInit {
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  async ngOnInit() {
    if (this.auth.isLoggedIn()) {
      const roles = this.auth.roles();
      if (roles.includes('ROLE_ADMIN')) await this.router.navigateByUrl('/admin/dashboard');
      else if (roles.includes('ROLE_DOCTOR')) await this.router.navigateByUrl('/doctor/dashboard');
      else if (roles.includes('ROLE_NURSE')) await this.router.navigateByUrl('/nurse/dashboard');
      else await this.router.navigateByUrl('/login');
    } else {
      await this.router.navigateByUrl('/login');
    }
  }

}