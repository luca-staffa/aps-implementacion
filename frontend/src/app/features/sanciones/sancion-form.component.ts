import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  computed,
  inject,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';

import { CalendarioService } from '../../core/calendario.service';
import { EscuderiasService } from '../../core/escuderias.service';
import {
  CATEGORIAS,
  Escuderia,
  EventoCalendario,
  Sancion,
  SancionPayload,
  TIPOS_SANCION,
  coincideCategoria,
} from '../../core/models';
import { Piloto, PilotosService } from '../../core/pilotos.service';

/** Exige que al menos uno de los dos campos tenga valor. */
function alMenosUno(campoA: string, campoB: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const valorA = grupo.get(campoA)?.value;
    const valorB = grupo.get(campoB)?.value;
    return valorA || valorB ? null : { pilotoOEscuderiaRequerido: true };
  };
}

@Component({
  selector: 'app-sancion-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="tarjeta formulario">
      <h3>{{ sancion ? 'Editar sancion' : 'Nueva sancion' }}</h3>

      @if (errorUsuarios()) {
        <div class="aviso error">{{ errorUsuarios() }}</div>
      }

      <form [formGroup]="form" (ngSubmit)="enviar()" novalidate>
        <div class="grilla">
          <div>
            <label for="s-categoria">Categoria</label>
            <select id="s-categoria" formControlName="categoria">
              <option value="">Selecciona una categoria</option>
              @for (categoria of categorias; track categoria.valor) {
                <option [value]="categoria.valor">{{ categoria.etiqueta }}</option>
              }
            </select>
            @if (mostrarError('categoria')) {
              <p class="error-campo">La categoria es obligatoria.</p>
            }
          </div>

          <div>
            <label for="s-carrera">Carrera</label>
            <select id="s-carrera" formControlName="eventoId">
              <option [ngValue]="null">Selecciona una carrera</option>
              @for (evento of eventosFiltrados(); track evento.id) {
                <option [ngValue]="evento.id">{{ etiquetaEvento(evento) }}</option>
              }
            </select>
            @if (mostrarError('eventoId')) {
              <p class="error-campo">La carrera es obligatoria.</p>
            }
          </div>

          <div>
            <label for="s-tipo">Penalidad</label>
            <select id="s-tipo" formControlName="tipo">
              <option value="">Selecciona una penalidad</option>
              @for (tipo of tipos; track tipo) {
                <option [value]="tipo">{{ tipo }}</option>
              }
            </select>
            @if (mostrarError('tipo')) {
              <p class="error-campo">La penalidad es obligatoria.</p>
            }
          </div>

          <div>
            <label for="s-piloto">Piloto</label>
            <select id="s-piloto" formControlName="pilotoId">
              <option [ngValue]="null">Sin piloto</option>
              @for (piloto of pilotos(); track piloto.id) {
                <option [ngValue]="piloto.id">{{ etiquetaPiloto(piloto) }}</option>
              }
            </select>
          </div>

          <div>
            <label for="s-escuderia">Escuderia</label>
            <select id="s-escuderia" formControlName="escuderiaId">
              <option [ngValue]="null">Sin escuderia</option>
              @for (escuderia of escuderias(); track escuderia.id) {
                <option [ngValue]="escuderia.id">{{ escuderia.nombre }}</option>
              }
            </select>
          </div>

          <div>
            <label for="s-fecha">Fecha</label>
            <input id="s-fecha" type="date" formControlName="fecha" />
            @if (mostrarError('fecha')) {
              <p class="error-campo">La fecha es obligatoria.</p>
            }
          </div>

          <div>
            <label for="s-motivo">Motivo</label>
            <input id="s-motivo" type="text" formControlName="motivo" placeholder="Motivo de la sancion" />
            @if (mostrarError('motivo')) {
              <p class="error-campo">El motivo es obligatorio.</p>
            }
          </div>
        </div>

        @if (form.hasError('pilotoOEscuderiaRequerido') && (form.touched || enviado)) {
          <div class="aviso error">Debes indicar al menos un piloto o una escuderia.</div>
        }

        <div class="descripcion">
          <label for="s-descripcion">Descripcion</label>
          <textarea id="s-descripcion" rows="3" formControlName="descripcion"></textarea>
        </div>

        <div class="acciones-formulario">
          <button type="submit">{{ sancion ? 'Guardar cambios' : 'Registrar sancion' }}</button>
          <button type="button" class="secundario" (click)="cancelar.emit()">Cancelar</button>
        </div>
      </form>
    </section>
  `,
  styles: [
    `
      .formulario {
        margin-bottom: 1.25rem;
        border-left: 4px solid #6d4bc9;
      }

      .grilla {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 0.75rem;
      }

      .descripcion {
        margin-top: 0.75rem;
      }

      .acciones-formulario {
        display: flex;
        gap: 0.6rem;
        margin-top: 1rem;
      }
    `,
  ],
})
export class SancionFormComponent implements OnInit, OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly pilotosService = inject(PilotosService);
  private readonly escuderiasService = inject(EscuderiasService);
  private readonly calendarioService = inject(CalendarioService);

  @Input() sancion: Sancion | null = null;
  @Output() guardar = new EventEmitter<SancionPayload>();
  @Output() cancelar = new EventEmitter<void>();

  readonly categorias = CATEGORIAS;
  readonly tipos = TIPOS_SANCION;
  enviado = false;

  readonly pilotos = signal<Piloto[]>([]);
  readonly escuderias = signal<Escuderia[]>([]);
  readonly eventos = signal<EventoCalendario[]>([]);
  readonly errorUsuarios = signal('');

  readonly form = this.fb.group(
    {
      categoria: ['', Validators.required],
      eventoId: this.fb.control<number | null>(null, Validators.required),
      pilotoId: this.fb.control<number | null>(null),
      escuderiaId: this.fb.control<number | null>(null),
      motivo: ['', Validators.required],
      fecha: ['', Validators.required],
      tipo: ['', Validators.required],
      descripcion: [''],
    },
    { validators: [alMenosUno('pilotoId', 'escuderiaId')] },
  );

  readonly eventosFiltrados = computed(() => {
    const categoria = this.form.controls.categoria.value;
    return this.eventos().filter(
      (evento) => !categoria || coincideCategoria(categoria, evento.categoria),
    );
  });

  ngOnInit(): void {
    this.cargarPilotos();
    this.cargarEscuderias();
    this.cargarEventos();
  }

  ngOnChanges(cambios: SimpleChanges): void {
    if (cambios['sancion']) {
      this.enviado = false;
      this.form.reset({
        categoria: this.sancion?.categoria ?? '',
        eventoId: this.sancion?.eventoId ?? null,
        pilotoId: this.sancion?.pilotoId ?? null,
        escuderiaId: this.sancion?.escuderiaId ?? null,
        motivo: this.sancion?.motivo ?? '',
        fecha: this.sancion?.fecha ?? '',
        tipo: this.sancion?.tipo ?? '',
        descripcion: this.sancion?.descripcion ?? '',
      });
    }
  }

  mostrarError(campo: string): boolean {
    const control = this.form.get(campo);
    return !!control && control.invalid && (control.touched || this.enviado);
  }

  enviar(): void {
    this.enviado = true;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const payload: SancionPayload = {
      categoria: valores.categoria ?? '',
      eventoId: valores.eventoId ?? null,
      pilotoId: valores.pilotoId ?? null,
      escuderiaId: valores.escuderiaId ?? null,
      motivo: (valores.motivo ?? '').trim(),
      fecha: valores.fecha ?? '',
      tipo: valores.tipo ?? '',
      descripcion: (valores.descripcion ?? '').trim() || null,
    };

    this.guardar.emit(payload);
  }

  etiquetaPiloto(piloto: Piloto): string {
    return `${piloto.nombre} ${piloto.apellido} (#${piloto.numero})`;
  }

  etiquetaEvento(evento: EventoCalendario): string {
    const fecha = evento.fechaInicio ?? '';
    const circuito = evento.circuito ? ` - ${evento.circuito}` : '';
    return `${evento.categoria} | ${evento.nombre ?? 'Carrera'} | ${fecha}${circuito}`;
  }

  private cargarPilotos(): void {
    this.pilotosService.listar().subscribe({
      next: (pilotos) => this.pilotos.set(pilotos ?? []),
      error: () => this.errorUsuarios.set('No se pudieron cargar los pilotos.'),
    });
  }

  private cargarEscuderias(): void {
    this.escuderiasService.listar().subscribe({
      next: (escuderias) => this.escuderias.set(escuderias ?? []),
      error: () => this.errorUsuarios.set('No se pudieron cargar las escuderias.'),
    });
  }

  private cargarEventos(): void {
    this.calendarioService.listar().subscribe({
      next: (eventos) => this.eventos.set(eventos ?? []),
      error: () => this.errorUsuarios.set('No se pudieron cargar las carreras.'),
    });
  }
}
