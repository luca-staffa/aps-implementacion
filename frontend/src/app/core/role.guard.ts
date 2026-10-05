import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';
import { Rol } from './models';

/** Guarda funcional que permite el acceso solo a los roles indicados. */
export function roleGuard(...roles: Rol[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const rol = auth.rol();

    if (rol && roles.includes(rol)) {
      return true;
    }

    return router.createUrlTree(['/dashboard']);
  };
}
