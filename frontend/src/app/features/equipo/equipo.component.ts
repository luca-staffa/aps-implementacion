import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { EquipoService } from '../../core/equipo.service';
import { MiembroEscuderia, ROLES_ESCUDERIA, RolEscuderia } from '../../core/models';

@Component({
  selector: 'app-equipo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Personal y roles</h1>
    <p class="intro">
      Asignacion y revocacion de roles internos de tu escuderia. El rol determina quien puede
      actualizar la informacion de la escuderia. Solo se administran los miembros de tu propia escuderia.
    </p>

    <section class="tarjeta">
      <div class="referencia">
        @for (rol of roles; track rol.valor) {
          <div class="rol">
            <span class="nombre">{{ rol.etiqueta }}</span>
            <span class="descripcion">{{ rol.descripcion }}</span>
          </div>
        }
      </div>
    </section>

    @if (error()) {
      <div class="aviso error">{{ error() }}</div>
    }
    @if (exito()) {
      <div class="aviso exito">{{ exito() }}</div>
    }

    @if (cargando()) {
      <p class="cargando">Cargando miembros...</p>
    } @else if (miembros().length === 0) {
      <p class="vacio">No hay miembros registrados en tu escuderia.</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>Miembro</th>
            <th>Usuario</th>
            <th>Email</th>
            <th>Rol global</th>
            <th>Rol de escuderia</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (miembro of miembros(); track miembro.usuarioId) {
            <tr>
              <td>{{ miembro.nombre }} {{ miembro.apellido }}</td>
              <td>{{ miembro.username }}</td>
              <td>{{ miembro.email }}</td>
              <td>{{ miembro.rolGlobal }}</td>
              <td>
                <select
                  [ngModel]="miembro.rolEscuderia"
                  (ngModelChange)="asignar(miembro, $event)"
                  name="rol-{{ miembro.usuarioId }}"
                >
                  <option [ngValue]="null">Sin rol</option>
                  @for (rol of roles; track rol.valor) {
                    <option [ngValue]="rol.valor">{{ rol.etiqueta }}</option>
                  }
                </select>
              </td>
              <td class="acciones-fila">
                @if (miembro.rolEscuderia) {
                  <button type="button" class="peligro" (click)="revocar(miembro)">Revocar</button>
                } @else {
                  <span class="sin-accion">-</span>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
  styles: [
    `
      .intro { color: #5b5470; }
      .referencia { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
      .rol { border-left: 3px solid #6d4bc9; padding-left: 0.75rem; }
      .rol .nombre { display: block; font-weight: 700; color: #4b2a8a; }
      .rol .descripcion { font-size: 0.85rem; color: #5b5470; }
      table { width: 100%; margin-top: 1rem; }
      .acciones-fila { display: flex; gap: 0.5rem; }
      .sin-accion { color: #5b5470; }
      select { min-width: 160px; }
    `,
  ],
})
export class EquipoComponent implements OnInit {
  private readonly servicio = inject(EquipoService);

  readonly roles = ROLES_ESCUDERIA;
  readonly miembros = signal<MiembroEscuderia[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly exito = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.servicio.listarMiembros().subscribe({
      next: (miembros) => this.miembros.set(miembros ?? []),
      error: () => {
        this.error.set('No se pudieron cargar los miembros de la escuderia.');
        this.miembros.set([]);
      },
      complete: () => this.cargando.set(false),
    });
  }

  asignar(miembro: MiembroEscuderia, rol: RolEscuderia | null): void {
    this.error.set('');
    this.exito.set('');
    if (!rol) {
      this.revocar(miembro);
      return;
    }
    this.servicio.asignarRol(miembro.usuarioId, rol).subscribe({
      next: (actualizado) => {
        this.actualizarMiembro(actualizado);
        this.exito.set('Rol actualizado para ' + actualizado.username + '.');
      },
      error: (err) => this.error.set(this.extraerMensajeError(err) ?? 'No se pudo asignar el rol.'),
    });
  }

  revocar(miembro: MiembroEscuderia): void {
    this.error.set('');
    this.exito.set('');
    this.servicio.revocarRol(miembro.usuarioId).subscribe({
      next: (actualizado) => {
        this.actualizarMiembro(actualizado);
        this.exito.set('Rol revocado a ' + actualizado.username + '.');
      },
      error: (err) => this.error.set(this.extraerMensajeError(err) ?? 'No se pudo revocar el rol.'),
    });
  }

  private actualizarMiembro(actualizado: MiembroEscuderia): void {
    this.miembros.set(
      this.miembros().map((miembro) =>
        miembro.usuarioId === actualizado.usuarioId ? actualizado : miembro,
      ),
    );
  }

  private extraerMensajeError(error: any): string | null {
    const payload = error?.error ?? error;
    if (typeof payload?.mensaje === 'string' && payload.mensaje.trim()) {
      return payload.mensaje;
    }
    if (typeof payload?.message === 'string' && payload.message.trim()) {
      return payload.message;
    }
    return null;
  }
}
