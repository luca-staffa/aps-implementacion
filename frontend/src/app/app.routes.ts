import { Routes } from '@angular/router';

import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'login',
    title: 'Ingreso · FIA',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'dashboard',
    title: 'Panel · FIA',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'calendario',
    title: 'Calendario · FIA',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/calendario/calendario.component').then((m) => m.CalendarioComponent),
  },
  {
    path: 'sanciones',
    title: 'Sanciones · FIA',
    canActivate: [authGuard, roleGuard('ADMIN_FIA', 'ADMIN_ESCUDERIA')],
    loadComponent: () =>
      import('./features/sanciones/sanciones.component').then((m) => m.SancionesComponent),
  },
  {
    path: 'pruebas-neumaticos',
    title: 'Pruebas de neumaticos · FIA',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/pruebas-neumaticos/pruebas.component').then((m) => m.PruebasComponent),
  },
  { path: '**', redirectTo: 'dashboard' },
];
