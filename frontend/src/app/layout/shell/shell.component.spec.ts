import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { BreakpointObserver } from '@angular/cdk/layout';
import { provideRouter } from '@angular/router';

import { ShellComponent, NAV_ITEMS } from './shell.component';
import { AuthService } from '../../auth/auth.service';

const mkToken = (roles: string[]) =>
  `h.${btoa(JSON.stringify({ realm_access: { roles } }))}.s`;

describe('ShellComponent', () => {
  const authMock = {
    getToken: jasmine.createSpy('getToken'),
    roleBaseRoute: jasmine.createSpy('roleBaseRoute'),
    roles: jasmine.createSpy('roles'),
    roleSettingsRoute: () => '/settings',
    username: () => 'test',
    roleLabel: () => 'User',
    logout: () => {}
  };

  beforeEach(async () => {
    authMock.roles.and.returnValue([]);
    authMock.roleBaseRoute.and.returnValue('/nurse');

    await TestBed.configureTestingModule({
      imports: [ShellComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authMock },
        {
          provide: BreakpointObserver,
          useValue: { observe: () => of({ matches: false }) }
        }
      ]
    }).compileComponents();
  });

  it('ROLE_NURSE should see Patients, Appointments, Beds only', () => {
    authMock.getToken.and.returnValue(mkToken(['ROLE_NURSE']));
    authMock.roleBaseRoute.and.returnValue('/nurse');
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const labels = component.visibleNavItems.map(i => i.label);
    expect(labels).toContain('Patients');
    expect(labels).toContain('Appointments');
    expect(labels).toContain('Beds');
    expect(labels).not.toContain('Analytics');
    expect(labels).not.toContain('Admin Settings');
    expect(labels).not.toContain('Audit Log');
  });

  it('ROLE_ADMIN should see all menu items', () => {
    authMock.getToken.and.returnValue(mkToken(['ROLE_ADMIN']));
    authMock.roleBaseRoute.and.returnValue('/admin');
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.visibleNavItems.length).toBe(NAV_ITEMS.length);
  });

  it('ROLE_DOCTOR should NOT see Analytics or Admin Settings', () => {
    authMock.getToken.and.returnValue(mkToken(['ROLE_DOCTOR']));
    authMock.roleBaseRoute.and.returnValue('/doctor');
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const labels = component.visibleNavItems.map(i => i.label);
    expect(labels).not.toContain('Analytics');
    expect(labels).not.toContain('Admin Settings');
  });
});
