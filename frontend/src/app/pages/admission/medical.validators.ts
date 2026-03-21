import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function validAge(min: number, max: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;
    const birthDate = new Date(value);
    const age = new Date().getFullYear() - birthDate.getFullYear();
    return age >= min && age <= max ? null : { invalidAge: { min, max, actual: age } };
  };
}

export const indianPhoneNumber: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;
  if (!value) return null;
  const regex = /^[+]?[\d\s\-()]{7,15}$/;
  return regex.test(value) ? null : { phone: true };
};

export const pinCode: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;
  if (!value) return null;
  const regex = /^\d{6}$/;
  return regex.test(value) ? null : { pinCode: true };
};

export const bloodGroupValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;
  if (!value) return null;
  const validGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  return validGroups.includes(value) ? null : { bloodGroup: true };
};
