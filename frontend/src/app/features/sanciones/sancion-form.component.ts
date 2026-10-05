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

import { CATEGORIAS, Sancion, SancionPayload, TIPOS_SANCION, Usuario } from '../../core/models';
import { UsuariosService } from '../../core/usuarios.service';

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
            <label for="s-tipo">Tipo</label>
            <select id="s-tipo" formControlName="tipo">
              <option value="">Selecciona un tipo</option>
              @for (tipo of tipos; track tipo) {
                <option [value]="tipo">{{ tipo }}</option>
              }
            </select>
            @if (mostrarError('tipo')) {
              <p class="error-campo">El tipo es obligatorio.</p>
            }
          </div>

          <div>
            <label for="s-piloto">Piloto</label>
            <select id="s-piloto" formControlName="pilotoId">
              <option [ngValue]="null">Sin piloto</option>
              @for (usuario of pilotos(); track usuario.id) {
                <option [ngValue]="usuario.id">{{ etiquetaUsuario(usuario) }}</option>
              }
            </select>
          </div>

          <div>
            <label for="s-escuderia">Escuderia</label>
            <select id="s-escuderia" formControlName="escuderiaId">
              <option [ngValue]="null">Sin escuderia</option>
              @for (usuario of escuderias(); track usuario.id) {
                <option [ngValue]="usuario.id">{{ etiquetaUsuario(usuario) }}</option>
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
  private readonly usuariosService = inject(UsuariosService);

  @Input() sancion: Sancion | null = null;
  @Output() guardar = new EventEmitter<SancionPayload>();
  @Output() cancelar = new EventEmitter<void>();

  readonly categorias = CATEGORIAS;
  readonly tipos = TIPOS_SANCION;
  enviado = false;

  readonly usuarios = signal<Usuario[]>([]);
  readonly errorUsuarios = signal('');
  readonly pilotos = computed(() => this.usuarios());
  readonly escuderias = computed(() =>
    this.usuarios().filter((usuario) => usuario.rol === 'ADMIN_ESCUDERIA'),
  );

  readonly form = this.fb.group(
    {
      categoria: ['', Validators.required],
      pilotoId: this.fb.control<number | null>(null),
      escuderiaId: this.fb.control<number | null>(null),
      motivo: ['', Validators.required],
      fecha: ['', Validators.required],
      tipo: ['', Validators.required],
      descripcion: [''],
    },
    { validators: [alMenosUno('pilotoId', 'escuderiaId')] },
  );

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  ngOnChanges(cambios: SimpleChanges): void {
    if (cambios['sancion']) {
      this.enviado = false;
      this.form.reset({
        categoria: this.sancion?.categoria ?? '',
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
      pilotoId: valores.pilotoId ?? null,
      escuderiaId: valores.escuderiaId ?? null,
      motivo: (valores.motivo ?? '').trim(),
      fecha: valores.fecha ?? '',
      tipo: valores.tipo ?? '',
      descripcion: (valores.descripcion ?? '').trim() || null,
    };

    this.guardar.emit(payload);
  }

  etiquetaUsuario(usuario: Usuario): string {
    const nombre = [usuario.nombre, usuario.apellido].filter(Boolean).join(' ');
    return `${nombre} (${usuario.username}) - ${usuario.rol}`;
  }

  private cargarUsuarios(): void {
    this.usuariosService.listar().subscribe({
      next: (usuarios) => this.usuarios.set(usuarios ?? []),
      error: () => this.errorUsuarios.set('No se pudieron cargar los usuarios.'),
    });
  }
}
