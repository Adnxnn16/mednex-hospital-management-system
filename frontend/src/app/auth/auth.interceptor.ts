import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError, switchMap } from 'rxjs';
import { AuthService } from './auth.service';
import { SessionService } from '../core/services/session.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const sessionService = inject(SessionService);
  const isApiRequest = req.url.includes('/api/') || req.url.startsWith('api/');
  const isExternalRequest = req.url.startsWith('http') && !req.url.includes('localhost:8081'); 

  let authReq = req;
  if (isApiRequest && !isExternalRequest) {
    const token = auth.accessToken();
    const tenant = auth.tenant();

    if (token) {
      sessionService.startExpiryWatch(token);
      authReq = authReq.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
    if (tenant) {
      authReq = authReq.clone({ setHeaders: { 'X-Tenant': tenant } });
    }
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && isApiRequest && !isExternalRequest) {
        return auth.refreshToken().pipe(
          switchMap((newToken) => {
            sessionService.startExpiryWatch(newToken);
            const retryReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` }
            });
            return next(retryReq);
          }),
          catchError((err) => {
            sessionService.logout();
            return throwError(() => err);
          })
        );
      }
      if (error.status === 401) {
        sessionService.logout();
      }
      return throwError(() => error);
    })
  );
};