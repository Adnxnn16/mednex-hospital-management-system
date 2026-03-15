import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  hidePassword = true;
  isLoading = false;
  errorMessage = '';

  roles = [
    { label: 'Admin',  hint: 'admin'  as const, icon: 'admin_panel_settings', desc: 'Hospital Administrator' },
    { label: 'Doctor', hint: 'doctor' as const, icon: 'stethoscope',          desc: 'Physician / Specialist' },
    { label: 'Nurse',  hint: 'nurse'  as const, icon: 'medical_services',     desc: 'Clinical Staff' },
  ];
  selectedRoleIndex = 1; // Default to Doctor

  constructor(
    private fb: FormBuilder,
    private router: Router,
    public auth: AuthService
  ) {
    this.loginForm = this.fb.group({
      rememberMe: [false]
    });
  }

  ngOnInit(): void {
    // If already authenticated, redirect straight to the role dashboard
    if (this.auth.isLoggedIn()) {
      this.auth.navigateToRoleDashboard();
    }
  }

  onRoleChange(index: number): void {
    this.selectedRoleIndex = index;
    this.errorMessage = '';
  }

  get selectedRole() {
    return this.roles[this.selectedRoleIndex];
  }

  /**
   * Initiate Keycloak OIDC login flow.
   * The login_hint passed here tells Keycloak which role tab to pre-select.
   * After the OAuth callback, auth.initializer.ts handles the redirect.
   */
  onLogin(): void {
    this.isLoading = true;
    try {
      this.auth.login(this.selectedRole.hint);
    } catch {
      this.errorMessage = 'Failed to connect to the authentication server. Please try again.';
      this.isLoading = false;
    }
  }

  /**
   * DEV ONLY: bypass Keycloak and navigate directly by role for local UI testing.
   * This skips AuthGuard, so only works if guards allow it.
   */
  onDevBypass(): void {
    const hint = this.selectedRole.hint;
    let role: 'ROLE_ADMIN' | 'ROLE_DOCTOR' | 'ROLE_NURSE' = 'ROLE_DOCTOR';
    
    if (hint === 'admin') role = 'ROLE_ADMIN';
    else if (hint === 'nurse') role = 'ROLE_NURSE';

    this.auth.setMockRole(role);
    this.auth.navigateToRoleDashboard();
  }
}
