import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, finalize, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { Credenciales, RegistroDatos, Rol, Sesion, Usuario } from './models';

const CLAVE_SESION = 'fia.sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly base = `${environment.apiUrl}/auth`;

  private readonly sesionState = signal<Sesion | null>(this.leerSesion());

  readonly sesion = this.sesionState.asReadonly();
  readonly autenticado = computed(() => {
    const sesion = this.sesionState();
    return sesion !== null && !this.estaExpirada(sesion);
  });
  readonly rol = computed<Rol | null>(() => (this.autenticado() ? this.sesionState()!.rol : null));
  readonly nombre = computed(() => (this.autenticado() ? this.sesionState()!.username : ''));
  readonly esAdminFia = computed(() => this.rol() === 'ADMIN_FIA');
  readonly esAdminEscuderia = computed(() => this.rol() === 'ADMIN_ESCUDERIA');
  readonly puedeGestionarSanciones = computed(() => this.esAdminFia() || this.esAdminEscuderia());

  login(username: string, password: string): Observable<Sesion> {
    const credenciales: Credenciales = { username, password };
    return this.http.post<Sesion>(`${this.base}/login`, credenciales).pipe(
      tap((respuesta) => this.establecerSesion(respuesta)),
    );
  }

  register(datos: RegistroDatos): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.base}/register`, datos);
  }

  me(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.base}/me`);
  }

  actualizarDatos(datos: { email: string; telefono: string }): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.base}/me`, datos);
  }

  logout(): void {
    this.http
      .post<void>(`${this.base}/logout`, {})
      .pipe(
        finalize(() => {
          this.limpiarSesion();
          this.router.navigate(['/login']);
        }),
      )
      .subscribe({ next: () => undefined, error: () => undefined });
  }

  /** Token vigente o null si no hay sesion / esta expirada. */
  token(): string | null {
    const sesion = this.sesionState();
    if (!sesion || this.estaExpirada(sesion)) {
      return null;
    }
    return sesion.token;
  }

  limpiarSesion(): void {
    this.sesionState.set(null);
    try {
      localStorage.removeItem(CLAVE_SESION);
    } catch {
      /* almacenamiento no disponible */
    }
  }

  private establecerSesion(respuesta: Sesion): void {
    // El backend envia la duracion en milisegundos (no en segundos).
    const duracionMs = Number(respuesta.expiraEn) || 0;
    const sesion: Sesion = {
      ...respuesta,
      expiraEnAbsoluto: duracionMs > 0 ? Date.now() + duracionMs : undefined,
    };
    this.sesionState.set(sesion);
    try {
      localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    } catch {
      /* almacenamiento no disponible */
    }
  }

  private estaExpirada(sesion: Sesion): boolean {
    return sesion.expiraEnAbsoluto !== undefined && Date.now() >= sesion.expiraEnAbsoluto;
  }

  private leerSesion(): Sesion | null {
    try {
      const crudo = localStorage.getItem(CLAVE_SESION);
      if (!crudo) {
        return null;
      }
      const sesion = JSON.parse(crudo) as Sesion;
      if (this.estaExpirada(sesion)) {
        localStorage.removeItem(CLAVE_SESION);
        return null;
      }
      return sesion;
    } catch {
      return null;
    }
  }
}
