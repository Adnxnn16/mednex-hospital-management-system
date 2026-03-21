import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { AuthService } from '../auth.service';
import { keycloakIssuer } from '../auth.config';

export type LoginRole = 'admin' | 'doctor' | 'nurse';

const ROLE_CONFIG: Record<LoginRole, { label: string; icon: string; subtitle: string }> = {
  admin:  { label: 'Admin',        icon: 'admin_panel_settings', subtitle: 'Hospital Administrator' },
  doctor: { label: 'Doctor',       icon: 'stethoscope',          subtitle: 'Physician / Specialist' },
  nurse:  { label: 'Nurse',        icon: 'medical_services',     subtitle: 'Clinical Staff' }
};

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
  step: 1 | 2 = 1;
  loginForm: FormGroup;
  hidePin = true;
  isLoading = false;
  errorMessage = '';

  /** Current role from route or query; default to doctor */
  role: LoginRole = 'doctor';

  readonly roleConfig = ROLE_CONFIG;
  readonly roles: LoginRole[] = ['admin', 'doctor', 'nurse'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    public auth: AuthService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.auth.navigateToRoleDashboard();
      return;
    }

    this.route.data.subscribe(d => {
      const fromData = d['role'] as LoginRole | undefined;
      if (fromData && this.roles.includes(fromData)) {
        this.role = fromData;
        this.errorMessage = '';
        this.step = 2; // Auto-skip to step 2 if route has specific role
        return;
      }
    });

    this.route.queryParams.subscribe(q => {
      const fromQuery = q['role'] as string | undefined;
      if (fromQuery && this.roles.includes(fromQuery as LoginRole)) {
        this.role = fromQuery as LoginRole;
        this.errorMessage = '';
        this.step = 2;
      }
    });
  }

  get roleLabel(): string {
    return this.role.charAt(0).toUpperCase() + this.role.slice(1);
  }

  get portalTitle(): string {
    return `${this.roleLabel} Portal Access`;
  }

  get buttonText(): string {
    return `Access ${this.roleLabel} Portal`;
  }

  get config() {
    return ROLE_CONFIG[this.role];
  }

  selectRole(r: LoginRole): void {
    this.role = r;
    this.errorMessage = '';
  }

  continueToStep2(): void {
    this.step = 2;
    this.errorMessage = '';
  }

  goToRoleSelector(): void {
    this.step = 1;
    this.errorMessage = '';
  }

  async onLogin(): Promise<void> {
    if (this.loginForm.invalid) {
      this.errorMessage = 'Please enter both your employee ID/email and security PIN.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    
    const { username, password } = this.loginForm.value;
    
    try {
      const success = await this.auth.loginWithPassword(username, password);
      if (success) {
        this.auth.navigateToRoleDashboard();
      } else {
         this.errorMessage = 'Invalid credentials. Please try again.';
      }
    } catch {
      this.errorMessage = 'Failed to connect to the authentication server. Please try again.';
    }

    this.isLoading = false;
  }

  openKeycloakAdmin(): void {
    window.open(`${keycloakIssuer.replace('/realms/mednex', '')}/admin`, '_blank');
  }
}
