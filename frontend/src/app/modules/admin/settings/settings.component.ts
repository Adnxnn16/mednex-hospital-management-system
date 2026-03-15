import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="settings-container animate-up">
      <div class="header">
        <h1>System Settings</h1>
        <p>Configure hospital preferences and security policies</p>
      </div>

      <div class="settings-grid">
        <div class="settings-card">
          <div class="section-head">
            <span class="material-symbols-outlined">account_circle</span>
            <h3>Profile Information</h3>
          </div>
          <div class="profile-box">
            <div class="avatar">Dev</div>
            <div class="info">
              <span class="name">{{ auth.username() }}</span>
              <span class="role">{{ auth.roleLabel() }}</span>
            </div>
          </div>
        </div>

        <div class="settings-card">
          <div class="section-head">
            <span class="material-symbols-outlined">hospital</span>
            <h3>Tenant Configuration</h3>
          </div>
          <div class="tenant-info">
            <p>Active Tenant: <strong> hospital_a</strong></p>
            <p>Environment: <strong>Enterprise Cloud</strong></p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .settings-container { padding: 32px; }
    .header { margin-bottom: 32px; }
    h1 { font-size: 28px; font-weight: 700; margin: 0 0 8px; }
    p { color: #94A3B8; }
    .settings-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
    .settings-card { background: #111827; border: 1px solid rgba(255,255,255,0.05); border-radius: 20px; padding: 24px; }
    .section-head { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    .section-head span { color: #22A3FF; }
    h3 { margin: 0; font-size: 16px; font-weight: 600; }
    .profile-box { display: flex; align-items: center; gap: 16px; }
    .avatar { width: 56px; height: 56px; border-radius: 12px; background: #22A3FF; display: flex; align-items: center; justify-content: center; font-weight: 700; color: white; }
    .info .name { display: block; font-weight: 600; font-size: 16px; color: white; }
    .info .role { font-size: 13px; color: #64748B; }
    .tenant-info p { color: #94A3B8; font-size: 14px; margin: 8px 0; }
    .tenant-info strong { color: white; }
  `]
})
export class SettingsComponent {
  constructor(public auth: AuthService) {}
}
