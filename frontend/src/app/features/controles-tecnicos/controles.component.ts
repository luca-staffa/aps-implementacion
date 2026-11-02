import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../core/auth.service';
import { ControlesService } from '../../core/controles.service';
import { EscuderiasService } from '../../core/escuderias.service';
import { ControlTecnico, ControlTecnicoPayload, Escuderia, ResultadoControl } from '../../core/models';

interface FormularioControl {
  escuderiaId: string;
  auto: string;
  fecha: string;
  elementosVerificados: string;
  resultado: ResultadoControl;
  observaciones: string;
}

@Component({
  selector: 'app-controles-tecnicos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Controles tecnicos</h1>
    <p class="intro">
      Registro de los controles tecnicos realizados a los autos de las escuderias. El administrativo
      de la FIA carga y modifica los controles; las escuderias y el publico pueden consultarlos.
    </p>

    <section class="tarjeta filtros">
      <div class="grilla-filtros">
        <div>
          <label for="f-escuderia">Escuderia</label>
          <select id="f-escuderia" [(ngModel)]="filtroEscuderiaId" (ngModelChange)="cargar()">
            <option value="">Todas</option>
            @for (escuderia of escuderias(); track escuderia.id) {
              <option [value]="escuderia.id">{{ escuderia.nombre }}</option>
            }
          </select>
        </div>

        <div class="acciones">
          @if (puedeGestionar()) {
            <button type="button" (click)="abrirFormulario()">Nuevo control</button>
          }
          <button type="button" class="secundario" (click)="limpiarFiltros()">Limpiar</button>
        </div>
      </div>
    </section>

    @if (mostrarFormulario()) {
      <section class="tarjeta formulario">
        <h2>{{ edicionId() ? 'Editar control' : 'Nuevo control' }}</h2>
        <div class="grilla-formulario">
          <label>
            Escuderia
            <select [(ngModel)]="form.escuderiaId" name="escuderiaId">
              <option value="">Seleccionar</option>
              @for (escuderia of escuderias(); track escuderia.id) {
                <option [value]="escuderia.id">{{ escuderia.nombre }}</option>
              }
            </select>
          </label>
          <label>
            Auto
            <input [(ngModel)]="form.auto" name="auto" placeholder="Ej. Ferrari SF-26 #99" />
          </label>
          <label>
            Fecha
            <input type="date" [(ngModel)]="form.fecha" name="fecha" />
          </label>
          <label>
            Resultado
            <select [(ngModel)]="form.resultado" name="resultado">
              <option value="APTO">Apto</option>
              <option value="NO_APTO">No apto</option>
            </select>
          </label>
          <label class="ancho">
            Elementos verificados
            <textarea rows="2" [(ngModel)]="form.elementosVerificados" name="elementosVerificados"></textarea>
          </label>
          <label class="ancho">
            Observaciones
            <textarea rows="2" [(ngModel)]="form.observaciones" name="observaciones"></textarea>
          </label>
        </div>

        <div class="acciones-form">
          <button type="button" (click)="guardar()">Guardar</button>
          <button type="button" class="secundario" (click)="cerrarFormulario()">Cancelar</button>
        </div>
      </section>
    }

    @if (error()) {
      <div class="aviso error">{{ error() }}</div>
    }

    @if (!puedeGestionar()) {
      <div class="aviso info">
        Tu perfil tiene acceso de solo lectura. Solo el administrativo de la FIA puede registrar o
        modificar controles tecnicos.
      </div>
    }

    @if (cargando()) {
      <p class="cargando">Cargando controles tecnicos...</p>
    } @else if (controles().length === 0) {
      <p class="vacio">No hay controles tecnicos registrados para los filtros seleccionados.</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Escuderia</th>
            <th>Auto</th>
            <th>Resultado</th>
            <th>Elementos verificados</th>
            <th>Observaciones</th>
            @if (puedeGestionar()) {
              <th>Acciones</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (control of controles(); track control.id) {
            <tr>
              <td>{{ control.fecha | date: 'dd/MM/yyyy' }}</td>
              <td>{{ control.escuderiaNombre || control.escuderiaId }}</td>
              <td>{{ control.auto }}</td>
              <td>
                <span class="etiqueta-resultado" [class.no-apto]="control.resultado === 'NO_APTO'">
                  {{ etiquetaResultado(control.resultado) }}
                </span>
              </td>
              <td>{{ control.elementosVerificados }}</td>
              <td>{{ control.observaciones || '-' }}</td>
              @if (puedeGestionar()) {
                <td class="acciones-fila">
                  <button type="button" class="secundario" (click)="editar(control)">Editar</button>
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
      .intro { color: #5b5470; }
      .filtros { margin-bottom: 1.25rem; }
      .grilla-filtros { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; align-items: end; }
      .acciones, .acciones-form { display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap; }
      .grilla-formulario { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
      .grilla-formulario .ancho { grid-column: 1 / -1; }
      label { display: flex; flex-direction: column; gap: 0.35rem; font-weight: 600; }
      .formulario { margin-bottom: 1.25rem; }
      table { width: 100%; }
      .acciones-fila { display: flex; gap: 0.5rem; }
      .etiqueta-resultado {
        display: inline-block;
        padding: 0.1rem 0.6rem;
        border-radius: 999px;
        font-size: 0.8rem;
        background: #e6f4ec;
        color: #1f8a52;
      }
      .etiqueta-resultado.no-apto {
        background: #fce8e6;
        color: #b3261e;
      }
    `,
  ],
})
export class ControlesTecnicosComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly servicio = inject(ControlesService);
  private readonly escuderiasService = inject(EscuderiasService);

  /** Solo el administrador de la FIA puede registrar o modificar controles. */
  readonly puedeGestionar = computed(() => this.auth.esAdminFia());

  readonly controles = signal<ControlTecnico[]>([]);
  readonly escuderias = signal<Escuderia[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly mostrarFormulario = signal(false);
  readonly edicionId = signal<number | null>(null);

  filtroEscuderiaId: number | string = '';

  form: FormularioControl = this.formularioVacio();

  ngOnInit(): void {
    this.cargarEscuderias();
    this.cargar();
  }

  cargarEscuderias(): void {
    this.escuderiasService.listar().subscribe({
      next: (escuderias) => this.escuderias.set(escuderias ?? []),
      error: () => this.escuderias.set([]),
    });
  }

  cargar(): void {
    const escuderiaId = this.filtroEscuderiaId ? Number(this.filtroEscuderiaId) : undefined;
    this.cargando.set(true);
    this.error.set('');

    this.servicio.listar(escuderiaId).subscribe({
      next: (controles) => this.controles.set(controles ?? []),
      error: () => {
        this.error.set('No se pudieron cargar los controles tecnicos.');
        this.controles.set([]);
      },
      complete: () => this.cargando.set(false),
    });
  }

  abrirFormulario(): void {
    if (!this.puedeGestionar()) {
      return;
    }
    this.form = this.formularioVacio();
    this.edicionId.set(null);
    this.error.set('');
    this.mostrarFormulario.set(true);
  }

  editar(control: ControlTecnico): void {
    if (!this.puedeGestionar()) {
      return;
    }
    this.edicionId.set(control.id);
    this.form = {
      escuderiaId: String(control.escuderiaId),
      auto: control.auto,
      fecha: control.fecha ? control.fecha.split('T')[0] : '',
      elementosVerificados: control.elementosVerificados,
      resultado: control.resultado,
      observaciones: control.observaciones ?? '',
    };
    this.error.set('');
    this.mostrarFormulario.set(true);
  }

  cerrarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.edicionId.set(null);
    this.form = this.formularioVacio();
  }

  guardar(): void {
    this.error.set('');
    if (!this.puedeGestionar()) {
      this.error.set('Tu perfil solo puede consultar controles tecnicos.');
      return;
    }
    if (!this.form.escuderiaId || !this.form.auto || !this.form.fecha || !this.form.elementosVerificados) {
      this.error.set('Completa escuderia, auto, fecha y elementos verificados antes de guardar.');
      return;
    }

    const payload: ControlTecnicoPayload = {
      escuderiaId: Number(this.form.escuderiaId),
      auto: this.form.auto,
      fecha: this.form.fecha,
      elementosVerificados: this.form.elementosVerificados,
      resultado: this.form.resultado,
      observaciones: this.form.observaciones || null,
    };

    const peticion = this.edicionId()
      ? this.servicio.actualizar(this.edicionId()!, payload)
      : this.servicio.crear(payload);

    peticion.subscribe({
      next: () => {
        this.cerrarFormulario();
        this.cargar();
      },
      error: (err) => this.error.set(this.extraerMensajeError(err) ?? 'No se pudo guardar el control tecnico.'),
    });
  }

  limpiarFiltros(): void {
    this.filtroEscuderiaId = '';
    this.cargar();
  }

  etiquetaResultado(resultado: ResultadoControl): string {
    return resultado === 'NO_APTO' ? 'No apto' : 'Apto';
  }

  private formularioVacio(): FormularioControl {
    return {
      escuderiaId: '',
      auto: '',
      fecha: '',
      elementosVerificados: '',
      resultado: 'APTO',
      observaciones: '',
    };
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
