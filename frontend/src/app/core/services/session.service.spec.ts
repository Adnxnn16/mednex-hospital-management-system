import { fakeAsync, TestBed, tick, discardPeriodicTasks } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { SessionService } from './session.service';

describe('SessionService', () => {
  let service: SessionService;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);
    snackBarSpy.open.and.returnValue({
      onAction: () => ({ subscribe: () => {} })
    } as any);

    TestBed.configureTestingModule({
      providers: [
        SessionService,
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } }
      ]
    });
    service = TestBed.inject(SessionService);
  });

  it('should show expiry warning when token has less than 5 minutes', fakeAsync(() => {
    const expIn4min = Math.floor(Date.now() / 1000) + 4 * 60;
    const fakeToken = `header.${btoa(JSON.stringify({ exp: expIn4min }))}.sig`;

    service.startExpiryWatch(fakeToken);
    tick(31_000);

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      jasmine.stringMatching(/expires in/),
      'Refresh Session',
      jasmine.any(Object)
    );
    discardPeriodicTasks();
  }));
});
