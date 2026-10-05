import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../core/auth.service';
import { CATEGORIAS, FiltrosSanciones, Sancion, SancionPayload } from '../../core/models';
import { SancionesService } from '../../core/sanciones.service';
import { SancionFormComponent } from './sancion-form.component';

@Component({
  selector: 'app-sanciones',
  standalone: true,
  imports: [CommonModule, FormsModule, SancionFormComponent],
  template: `
    <h1>Sanciones</h1>
    <p class="intro">
      @if (auth.esAdminFia()) {
        Como Administrador FIA puedes registrar, modificar y eliminar sanciones.
      } @else {
        Consulta las sanciones y confirma su lectura con el boton "Visto".
      }
    </p>

    <section class="tarjeta filtros">
      <div class="grilla-filtros">
        <div>
          <label for="f-categoria">Categoria</label>
          <select id="f-categoria" [(ngModel)]="filtros.categoria" (ngModelChange)="cargar()">
            <option value="">Todas</option>
            @for (categoria of categorias; track categoria.valor) {
              <option [value]="categoria.valor">{{ categoria.etiqueta }}</option>
            }
          </select>
        </div>

        <div>
          <label for="f-desde">Desde</label>
          <input id="f-desde" type="date" [(ngModel)]="filtros.desde" (ngModelChange)="cargar()" />
        </div>

        <div>
          <label for="f-hasta">Hasta</label>
          <input id="f-hasta" type="date" [(ngModel)]="filtros.hasta" (ngModelChange)="cargar()" />
        </div>

        <div class="acciones">
          <button type="button" class="secundario" (click)="limpiarFiltros()">Limpiar</button>
          @if (auth.esAdminFia()) {
            <button type="button" (click)="nueva()">Nueva sancion</button>
          }
        </div>
      </div>
    </section>

    @if (mostrarFormulario()) {
      <app-sancion-form
        [sancion]="enEdicion()"
        (guardar)="guardar($event)"
        (cancelar)="cerrarFormulario()"
      ></app-sancion-form>
    }

    @if (aviso()) {
      <div class="aviso exito">{{ aviso() }}</div>
    }

    @if (error()) {
      <div class="aviso error">{{ error() }}</div>
    }

    @if (cargando()) {
      <p class="cargando">Cargando sanciones...</p>
    } @else if (sanciones().length === 0) {
      <p class="vacio">No hay sanciones para los filtros seleccionados.</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>Categoria</th>
            <th>Piloto / Escuderia</th>
            <th>Motivo</th>
            <th>Fecha</th>
            <th>Tipo</th>
            <th>Visto</th>
            @if (auth.esAdminFia() || auth.esAdminEscuderia()) {
              <th>Acciones</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (sancion of sanciones(); track sancion.id) {
            <tr>
              <td>{{ sancion.categoria }}</td>
              <td>
                @if (sancion.pilotoNombre) {
                  <div>{{ sancion.pilotoNombre }}</div>
                }
                @if (sancion.escuderiaNombre) {
                  <div class="detalle">{{ sancion.escuderiaNombre }}</div>
                }
              </td>
              <td>
                <div>{{ sancion.motivo }}</div>
                @if (sancion.descripcion) {
                  <div class="detalle">{{ sancion.descripcion }}</div>
                }
              </td>
              <td>{{ sancion.fecha | date: 'dd/MM/yyyy' }}</td>
              <td>{{ sancion.tipo }}</td>
              <td>
                @if (sancion.cantidadVistas > 0) {
                  <span class="visto">
                    {{ sancion.vistas[0].fechaVisto | date: 'dd/MM/yyyy HH:mm' }}
                    @if (sancion.vistas[0].username) {
                      · {{ sancion.vistas[0].username }}
                    }
                  </span>
                } @else {
                  <span class="pendiente">Pendiente</span>
                }
              </td>
              @if (auth.esAdminFia() || auth.esAdminEscuderia()) {
                <td class="acciones-fila">
                  @if (auth.esAdminFia()) {
                    <button type="button" class="secundario" (click)="editar(sancion)">Editar</button>
                    <button type="button" class="peligro" (click)="eliminar(sancion)">Eliminar</button>
                  }
                  @if (auth.esAdminEscuderia() && sancion.cantidadVistas === 0) {
                    <button type="button" (click)="marcarVisto(sancion)">Visto</button>
                  }
                </td>
              }
            </tr>
          }
        </tbody>
      </table>
    }
  `,
  styles: [
    `
      .intro {
        color: #5b5470;
      }

      .filtros {
        margin-bottom: 1.25rem;
      }

      .acciones {
        display: flex;
        gap: 0.6rem;
        align-items: flex-end;
      }

      .detalle {
        font-size: 0.8rem;
        color: #5b5470;
      }

      .acciones-fila {
        display: flex;
        flex-wrap: wrap;
        gap: 0.4rem;
      }

      .visto {
        color: #1f8a52;
        font-size: 0.82rem;
      }

      .pendiente {
        color: #b8860b;
        font-size: 0.82rem;
      }

      table {
        box-shadow: 0 2px 10px rgba(43, 16, 85, 0.08);
        border-radius: 8px;
        overflow: hidden;
      }
    `,
  ],
})
export class SancionesComponent implements OnInit {
  private readonly servicio = inject(SancionesService);
  readonly auth = inject(AuthService);

  readonly categorias = CATEGORIAS;
  readonly sanciones = signal<Sancion[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly aviso = signal('');
  readonly mostrarFormulario = signal(false);
  readonly enEdicion = signal<Sancion | null>(null);

  filtros: Required<FiltrosSanciones> = { categoria: '', desde: '', hasta: '' };

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.servicio.listar(this.filtrosActuales()).subscribe({
      next: (sanciones) => this.sanciones.set(sanciones ?? []),
      error: () => {
        this.error.set('No se pudieron cargar las sanciones.');
        this.sanciones.set([]);
        this.cargando.set(false);
      },
      complete: () => this.cargando.set(false),
    });
  }

  limpiarFiltros(): void {
    this.filtros = { categoria: '', desde: '', hasta: '' };
    this.cargar();
  }

  nueva(): void {
    this.aviso.set('');
    this.enEdicion.set(null);
    this.mostrarFormulario.set(true);
  }

  editar(sancion: Sancion): void {
    this.aviso.set('');
    this.enEdicion.set(sancion);
    this.mostrarFormulario.set(true);
  }

  cerrarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.enEdicion.set(null);
  }

  guardar(payload: SancionPayload): void {
    const edicion = this.enEdicion();
    const peticion = edicion
      ? this.servicio.actualizar(edicion.id, payload)
      : this.servicio.crear(payload);

    this.error.set('');
    peticion.subscribe({
      next: () => {
        this.aviso.set(edicion ? 'Sancion actualizada correctamente.' : 'Sancion registrada correctamente.');
        this.cerrarFormulario();
        this.cargar();
      },
      error: () => this.error.set('No se pudo guardar la sancion.'),
    });
  }

  eliminar(sancion: Sancion): void {
    if (!window.confirm('¿Confirmas la eliminacion de esta sancion?')) {
      return;
    }

    this.error.set('');
    this.servicio.eliminar(sancion.id).subscribe({
      next: () => {
        this.aviso.set('Sancion eliminada correctamente.');
        this.cargar();
      },
      error: () => this.error.set('No se pudo eliminar la sancion.'),
    });
  }

  marcarVisto(sancion: Sancion): void {
    this.error.set('');
    this.servicio.marcarVisto(sancion.id).subscribe({
      next: (visto) => {
        const fecha = visto?.fechaVisto ?? 'la fecha registrada';
        const usuario = visto?.username ?? this.auth.nombre();
        this.aviso.set(`Sancion marcada como vista por ${usuario} (${fecha}).`);
        this.cargar();
      },
      error: () => this.error.set('No se pudo registrar el visto.'),
    });
  }

  private filtrosActuales(): FiltrosSanciones {
    return {
      categoria: this.filtros.categoria || undefined,
      desde: this.filtros.desde || undefined,
      hasta: this.filtros.hasta || undefined,
    };
  }
}