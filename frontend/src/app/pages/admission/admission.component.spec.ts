import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { AdmissionComponent } from './admission.component';
import { ApiService } from '../../shared/api.service';

describe('AdmissionComponent', () => {
  let component: AdmissionComponent;
  let fixture: ComponentFixture<AdmissionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, MatSnackBarModule, NoopAnimationsModule],
      providers: [
        {
          provide: ApiService,
          useValue: {
            listBeds: () => of([]),
            admitPatient: () => of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdmissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have at least 50 form controls', () => {
    expect(component.totalFormControlCount).toBeGreaterThanOrEqual(50);
  });
});
