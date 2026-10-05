import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    @if (auth.autenticado()) {
      <header class="barra">
        <div class="marca">FIA <span>Comision Violeta</span></div>

        <nav class="menu">
          <a routerLink="/dashboard" routerLinkActive="activo">Panel</a>
          <a routerLink="/calendario" routerLinkActive="activo">Calendario</a>
          @if (auth.puedeGestionarSanciones()) {
            <a routerLink="/sanciones" routerLinkActive="activo">Sanciones</a>
          }
          <a routerLink="/pruebas-neumaticos" routerLinkActive="activo">Pruebas de neumaticos</a>
        </nav>

        <div class="sesion">
          <div class="datos">
            <span class="nombre">{{ auth.nombre() }}</span>
            <span class="rol">{{ etiquetaRol(auth.rol()) }}</span>
          </div>
          <button type="button" class="secundario" (click)="cerrarSesion()">Cerrar sesion</button>
        </div>
      </header>
    }

    <main [class.con-barra]="auth.autenticado()">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [
    `
      .barra {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 1rem;
        padding: 0.75rem 1.5rem;
        background: #2b1055;
        color: #ffffff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      }

      .marca {
        font-weight: 700;
        letter-spacing: 0.05em;
        font-size: 1.05rem;
      }

      .marca span {
        font-weight: 400;
        font-size: 0.8rem;
        color: #b9a6ec;
        margin-left: 0.4rem;
      }

      .menu {
        display: flex;
        flex-wrap: wrap;
        gap: 0.35rem;
        flex: 1 1 auto;
      }

      .menu a {
        color: #e8e2f7;
        padding: 0.4rem 0.7rem;
        border-radius: 6px;
        font-size: 0.9rem;
      }

      .menu a:hover {
        background: rgba(255, 255, 255, 0.12);
        text-decoration: none;
      }

      .menu a.activo {
        background: #6d4bc9;
        color: #ffffff;
      }

      .sesion {
        display: flex;
        align-items: center;
        gap: 0.75rem;
      }

      .datos {
        display: flex;
        flex-direction: column;
        line-height: 1.2;
        text-align: right;
      }

      .nombre {
        font-weight: 600;
        font-size: 0.9rem;
      }

      .rol {
        font-size: 0.72rem;
        color: #b9a6ec;
      }

      main {
        display: block;
      }

      main.con-barra {
        padding: 1.5rem;
        max-width: 1200px;
        margin: 0 auto;
      }
    `,
  ],
})
export class AppComponent {
  readonly auth = inject(AuthService);

  cerrarSesion(): void {
    this.auth.logout();
  }

  etiquetaRol(rol: string | null): string {
    switch (rol) {
      case 'ADMIN_FIA':
        return 'Administrador FIA';
      case 'ADMIN_ESCUDERIA':
        return 'Administrador de escuderia';
      case 'PUBLICO':
        return 'Publico';
      default:
        return '';
    }
  }
}
