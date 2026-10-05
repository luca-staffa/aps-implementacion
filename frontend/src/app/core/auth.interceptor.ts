import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.token();
  const peticionAutenticada = token
    ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : peticion;

  return siguiente(peticionAutenticada).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        auth.limpiarSesion();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
