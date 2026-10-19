import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../core/auth.service';
import { CATEGORIAS } from '../../core/models';
import { Piloto, PilotoPayload, PilotosService } from '../../core/pilotos.service';

interface FormularioPiloto {
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  nacionalidad: string;
  numero: string;
  abreviatura: string;
  rol: 'TITULAR' | 'SUPLENTE';
  numeroLicenciaFia: string;
  fotoUrl: string;
  categoriaId: string;
  temporadaId: string;
  escuderiaId: string;
}

@Component({
  selector: 'app-pilotos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Pilotos</h1>
    <p class="intro">
      Gestion de la plantilla por escudería y validación de límites de titulares y suplentes.
    </p>

    <section class="tarjeta filtros">
      <div class="grilla-filtros">
        <div>
          <label for="f-categoria">Categoria</label>
          <select id="f-categoria" [(ngModel)]="filtroCategoria" (ngModelChange)="cargar()">
            <option value="">Todas</option>
            @for (categoria of categorias; track categoria.valor) {
              <option [value]="categoriaIds[categoria.valor]">{{ categoria.etiqueta }}</option>
            }
          </select>
        </div>

        <div>
          <label for="f-temporada">Temporada</label>
          <select id="f-temporada" [(ngModel)]="filtroTemporada" (ngModelChange)="cargar()">
            <option value="">Todas</option>
            @for (temporada of temporadas; track temporada.id) {
              <option [value]="temporada.id">{{ temporada.anio }}</option>
            }
          </select>
        </div>

        @if (!auth.esAdminFia()) {
          <div class="acciones">
            <button type="button" (click)="abrirFormulario()">Nuevo piloto</button>
            <button type="button" class="secundario" (click)="limpiarFiltros()">Limpiar</button>
          </div>
        }
      </div>
    </section>

    @if (mostrarFormulario()) {
      <section class="tarjeta formulario">
        <h2>{{ edicionId() ? 'Editar piloto' : 'Nuevo piloto' }}</h2>
        <div class="grilla-formulario">
          <label>
            Nombre
            <input [(ngModel)]="form.nombre" name="nombre" />
            @if (errorDeCampo('nombre')) {
              <small class="error-campo">{{ errorDeCampo('nombre') }}</small>
            }
          </label>
          <label>
            Apellido
            <input [(ngModel)]="form.apellido" name="apellido" />
            @if (errorDeCampo('apellido')) {
              <small class="error-campo">{{ errorDeCampo('apellido') }}</small>
            }
          </label>
          <label>
            Fecha de nacimiento
            <input type="date" [(ngModel)]="form.fechaNacimiento" name="fechaNacimiento" />
            @if (errorDeCampo('fechaNacimiento')) {
              <small class="error-campo">{{ errorDeCampo('fechaNacimiento') }}</small>
            }
          </label>
          <label>
            Nacionalidad
            <input [(ngModel)]="form.nacionalidad" name="nacionalidad" />
            @if (errorDeCampo('nacionalidad')) {
              <small class="error-campo">{{ errorDeCampo('nacionalidad') }}</small>
            }
          </label>
          <label>
            Numero
            <input type="number" min="1" [(ngModel)]="form.numero" name="numero" />
            @if (errorDeCampo('numero')) {
              <small class="error-campo">{{ errorDeCampo('numero') }}</small>
            }
          </label>
          <label>
            Abreviatura
            <input [(ngModel)]="form.abreviatura" name="abreviatura" maxlength="3" />
            @if (errorDeCampo('abreviatura')) {
              <small class="error-campo">{{ errorDeCampo('abreviatura') }}</small>
            }
          </label>
          <label>
            Rol
            <select [(ngModel)]="form.rol" name="rol">
              <option value="TITULAR">Titular</option>
              <option value="SUPLENTE">Suplente</option>
            </select>
            @if (errorDeCampo('rol')) {
              <small class="error-campo">{{ errorDeCampo('rol') }}</small>
            }
          </label>
          <label>
            Numero de licencia FIA
            <input [(ngModel)]="form.numeroLicenciaFia" name="numeroLicenciaFia" />
            @if (errorDeCampo('numeroLicenciaFia')) {
              <small class="error-campo">{{ errorDeCampo('numeroLicenciaFia') }}</small>
            }
          </label>
          <label>
            Foto URL
            <input [(ngModel)]="form.fotoUrl" name="fotoUrl" />
            @if (errorDeCampo('fotoUrl')) {
              <small class="error-campo">{{ errorDeCampo('fotoUrl') }}</small>
            }
          </label>
          <label>
            Categoria
            <select [(ngModel)]="form.categoriaId" name="categoriaId">
              <option value="">Seleccionar</option>
              @for (categoria of categorias; track categoria.valor) {
                <option [value]="categoriaIds[categoria.valor]">{{ categoria.etiqueta }}</option>
              }
            </select>
            @if (errorDeCampo('categoriaId')) {
              <small class="error-campo">{{ errorDeCampo('categoriaId') }}</small>
            }
          </label>
          <label>
            Temporada
            <select [(ngModel)]="form.temporadaId" name="temporadaId">
              <option value="">Seleccionar</option>
              @for (temporada of temporadas; track temporada.id) {
                <option [value]="temporada.id">{{ temporada.anio }}</option>
              }
            </select>
            @if (errorDeCampo('temporadaId')) {
              <small class="error-campo">{{ errorDeCampo('temporadaId') }}</small>
            }
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

    @if (cargando()) {
      <p class="cargando">Cargando pilotos...</p>
    } @else if (pilotos().length === 0) {
      <p class="vacio">No hay pilotos para los filtros seleccionados.</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th>Piloto</th>
            <th>Categoria</th>
            <th>Temporada</th>
            <th>Escuderia</th>
            <th>Rol</th>
            <th>Numero</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (piloto of pilotos(); track piloto.id) {
            <tr>
              <td>{{ piloto.nombre }} {{ piloto.apellido }}</td>
              <td>{{ piloto.categoriaCodigo || piloto.categoriaId }}</td>
              <td>{{ piloto.temporadaAnio || piloto.temporadaId }}</td>
              <td>{{ piloto.escuderiaNombre || '-' }}</td>
              <td>{{ piloto.rol }}</td>
              <td>{{ piloto.numero }}</td>
              <td class="acciones-fila">
                @if (!auth.esAdminFia()) {
                  <button type="button" class="secundario" (click)="editar(piloto)">Editar</button>
                  <button type="button" class="peligro" (click)="eliminar(piloto)">Eliminar</button>
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
      .grilla-filtros { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; align-items: end; }
      .acciones, .acciones-form { display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap; }
      .grilla-formulario { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
      label { display: flex; flex-direction: column; gap: 0.35rem; font-weight: 600; }
      input, select { padding: 0.55rem 0.7rem; border: 1px solid #d9d2ee; border-radius: 8px; }
      .error-campo { color: #b42318; font-size: 0.75rem; font-weight: 500; }
      table { width: 100%; margin-top: 1rem; }
      .acciones-fila { display: flex; gap: 0.5rem; }
    `,
  ],
})
export class PilotosComponent {
  readonly auth = inject(AuthService);
  private readonly servicio = inject(PilotosService);

  readonly categorias = CATEGORIAS;
  readonly temporadas = [
    { id: 1, anio: 2026 },
    { id: 2, anio: 2025 },
    { id: 3, anio: 2024 },
  ];
  readonly categoriaIds: Record<string, number> = { F1: 1, F2: 2, F3: 3, F1_ACADEMY: 4 };
  readonly pilotos = signal<Piloto[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly erroresCampo = signal<Record<string, string>>({});
  readonly mostrarFormulario = signal(false);
  readonly edicionId = signal<number | null>(null);

  filtroCategoria = '';
  filtroTemporada = '';

  form: FormularioPiloto = {
    nombre: '',
    apellido: '',
    fechaNacimiento: '',
    nacionalidad: '',
    numero: '1',
    abreviatura: 'AAA',
    rol: 'TITULAR',
    numeroLicenciaFia: '',
    fotoUrl: '',
    categoriaId: '',
    temporadaId: '',
    escuderiaId: '',
  };

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    const categoriaId = this.filtroCategoria ? Number(this.filtroCategoria) : undefined;
    const temporadaId = this.filtroTemporada ? Number(this.filtroTemporada) : undefined;

    this.cargando.set(true);
    this.error.set('');
    this.servicio.listar({ categoriaId, temporadaId }).subscribe({
      next: (respuesta) => this.pilotos.set(respuesta ?? []),
      error: () => {
        this.error.set('No se pudieron cargar los pilotos.');
        this.pilotos.set([]);
      },
      complete: () => this.cargando.set(false),
    });
  }

  abrirFormulario(): void {
    if (this.auth.esAdminFia()) {
      return;
    }
    this.resetFormulario();
    this.edicionId.set(null);
    this.mostrarFormulario.set(true);
  }

  editar(piloto: Piloto): void {
    if (this.auth.esAdminFia()) {
      return;
    }
    this.edicionId.set(piloto.id ?? null);
    this.form = {
      nombre: piloto.nombre,
      apellido: piloto.apellido,
      fechaNacimiento: piloto.fechaNacimiento ? piloto.fechaNacimiento.split('T')[0] : '',
      nacionalidad: piloto.nacionalidad,
      numero: String(piloto.numero ?? ''),
      abreviatura: piloto.abreviatura,
      rol: piloto.rol,
      numeroLicenciaFia: piloto.numeroLicenciaFia,
      fotoUrl: piloto.fotoUrl ?? '',
      categoriaId: piloto.categoriaId ? String(piloto.categoriaId) : '',
      temporadaId: piloto.temporadaId ? String(piloto.temporadaId) : '',
      escuderiaId: piloto.escuderiaId ? String(piloto.escuderiaId) : '',
    };
    this.mostrarFormulario.set(true);
  }

  cerrarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.edicionId.set(null);
    this.resetFormulario();
  }

  guardar(): void {
    this.limpiarErrores();
    if (this.auth.esAdminFia()) {
      this.error.set('La FIA solo puede consultar pilotos.');
      return;
    }
    if (!this.form.nombre || !this.form.apellido || !this.form.categoriaId || !this.form.temporadaId) {
      this.error.set('Completa nombre, categoria y temporada antes de guardar.');
      return;
    }

    const payload: PilotoPayload = {
      nombre: this.form.nombre,
      apellido: this.form.apellido,
      fechaNacimiento: this.form.fechaNacimiento,
      nacionalidad: this.form.nacionalidad,
      numero: Number(this.form.numero) || 1,
      abreviatura: this.form.abreviatura.toUpperCase(),
      rol: this.form.rol,
      numeroLicenciaFia: this.form.numeroLicenciaFia,
      fotoUrl: this.form.fotoUrl || null,
      categoriaId: Number(this.form.categoriaId),
      temporadaId: Number(this.form.temporadaId),
      escuderiaId: null,
    };

    const peticion = this.edicionId() ? this.servicio.actualizar(this.edicionId()!, payload) : this.servicio.crear(payload);

    peticion.subscribe({
      next: () => {
        this.limpiarErrores();
        this.cerrarFormulario();
        this.cargar();
      },
      error: (err) => this.manejarErrorBackend(err, 'No se pudo guardar el piloto.'),
    });
  }

  eliminar(piloto: Piloto): void {
    this.limpiarErrores();
    if (this.auth.esAdminFia()) {
      return;
    }
    if (!window.confirm('¿Deseas eliminar este piloto?')) {
      return;
    }
    this.servicio.eliminar(piloto.id!).subscribe({
      next: () => this.cargar(),
      error: (err) => this.manejarErrorBackend(err, 'No se pudo eliminar el piloto.'),
    });
  }

  errorDeCampo(campo: string): string {
    return this.erroresCampo()[campo] ?? '';
  }

  limpiarFiltros(): void {
    this.filtroCategoria = '';
    this.filtroTemporada = '';
    this.cargar();
  }

  private resetFormulario(): void {
    this.limpiarErrores();
    this.form = {
      nombre: '',
      apellido: '',
      fechaNacimiento: '',
      nacionalidad: '',
      numero: '1',
      abreviatura: 'AAA',
      rol: 'TITULAR',
      numeroLicenciaFia: '',
      fotoUrl: '',
      categoriaId: '',
      temporadaId: '',
      escuderiaId: '',
    };
  }

  private limpiarErrores(): void {
    this.error.set('');
    this.erroresCampo.set({});
  }

  private manejarErrorBackend(error: any, fallback: string): void {
    const mensaje = this.extraerMensajeError(error);
    const detalleCampo = this.extraerDetalleCampoError(mensaje);

    if (detalleCampo) {
      this.error.set('');
      this.erroresCampo.set({ ...this.erroresCampo(), [detalleCampo.campo]: detalleCampo.detalle });
    } else {
      this.erroresCampo.set({});
      this.error.set(mensaje ?? fallback);
    }
  }

  private extraerMensajeError(error: any): string | null {
    const payload = error?.error ?? error;
    if (typeof payload?.mensaje === 'string' && payload.mensaje.trim()) {
      return payload.mensaje;
    }
    if (typeof payload?.message === 'string' && payload.message.trim()) {
      return payload.message;
    }
    if (typeof payload === 'string' && payload.trim()) {
      return payload;
    }
    return null;
  }

  private extraerDetalleCampoError(mensaje: string | null): { campo: string; detalle: string } | null {
    if (!mensaje) {
      return null;
    }

    const coincidencia = mensaje.match(/^([a-zA-Z0-9_]+)\s*:\s*(.+)$/);
    if (!coincidencia) {
      return null;
    }

    const campo = coincidencia[1];
    const detalle = coincidencia[2].trim();
    const camposPermitidos = new Set([
      'nombre',
      'apellido',
      'fechaNacimiento',
      'nacionalidad',
      'numero',
      'abreviatura',
      'rol',
      'numeroLicenciaFia',
      'fotoUrl',
      'categoriaId',
      'temporadaId',
    ]);

    return camposPermitidos.has(campo) ? { campo, detalle } : null;
  }
}
