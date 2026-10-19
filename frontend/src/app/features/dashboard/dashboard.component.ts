import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth.service';

interface AccesoDirecto {
  titulo: string;
  descripcion: string;
  ruta: string;
  visible: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <h1>Panel</h1>
    <p class="bienvenida">
      Bienvenido/a, <strong>{{ auth.nombre() }}</strong>. Perfil activo:
      <strong>{{ etiquetaRol() }}</strong>.
    </p>

    <div class="aviso atencion">
      Por seguridad, la sesion se cierra automaticamente tras un periodo de inactividad. Si el
      servidor responde con error de autenticacion (401), la aplicacion te redirigira al inicio de
      sesion.
    </div>

    <div class="tarjetas">
      @for (acceso of accesos(); track acceso.ruta) {
        @if (acceso.visible) {
          <article class="tarjeta acceso">
            <h3>{{ acceso.titulo }}</h3>
            <p>{{ acceso.descripcion }}</p>
            <a [routerLink]="acceso.ruta">Ir</a>
          </article>
        }
      }
    </div>
  `,
  styles: [
    `
      h1 {
        margin-bottom: 0.3rem;
      }

      .bienvenida {
        color: #5b5470;
      }

      .tarjetas {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: 1rem;
        margin-top: 1.25rem;
      }

      .acceso h3 {
        margin-bottom: 0.35rem;
      }

      .acceso p {
        color: #5b5470;
        font-size: 0.9rem;
        min-height: 2.8rem;
      }

      .acceso a {
        font-weight: 600;
      }
    `,
  ],
})
export class DashboardComponent {
  readonly auth = inject(AuthService);

  readonly accesos: () => AccesoDirecto[] = () => [
    {
      titulo: 'Calendario',
      descripcion:
        'Consulta los eventos de las cuatro categorias, filtra por fecha o circuito y descarga el calendario en PDF o ICS.',
      ruta: '/calendario',
      visible: true,
    },
    {
      titulo: 'Vista consolidada',
      descripcion:
        'Consulta el resumen operacional por categoria y temporada con los indicadores clave para la FIA.',
      ruta: '/dashboard/consolidado',
      visible: this.auth.esAdminFia(),
    },
    {
      titulo: 'Pilotos',
      descripcion:
        'Administra la plantilla de pilotos por escuderia, valida titulares y suplentes, y revisa el estado de cada categoria.',
      ruta: '/pilotos',
      visible: this.auth.esAdminFia() || this.auth.esAdminEscuderia(),
    },
    {
      titulo: 'Sanciones',
      descripcion: this.auth.esAdminFia()
        ? 'Registra, modifica y elimina sanciones. La confirmacion de "Visto" corresponde a las escuderias.'
        : 'Consulta las sanciones de tu escuderia y confirma la lectura con el boton "Visto".',
      ruta: '/sanciones',
      visible: this.auth.puedeGestionarSanciones(),
    },
    {
      titulo: 'Pilotos publicos',
      descripcion: 'Consulta la base publica de pilotos con filtros por categoria, temporada y escuderia.',
      ruta: '/pilotos/publico',
      visible: true,
    },
    {
      titulo: 'Pruebas de neumaticos',
      descripcion:
        'Revisa las dos fechas anuales, el circuito, las categorias participantes y los resultados por escuderia.',
      ruta: '/pruebas-neumaticos',
      visible: true,
    },
  ];

  etiquetaRol(): string {
    switch (this.auth.rol()) {
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
