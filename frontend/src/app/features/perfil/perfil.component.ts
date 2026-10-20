import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Mis datos personales</h1>
    <p class="intro">
      Actualiza tu correo electronico y telefono. Solo podes modificar los datos de tu propia cuenta.
    </p>

    <section class="tarjeta panel">
      @if (cargando()) {
        <p class="cargando">Cargando datos...</p>
      } @else {
        <form [formGroup]="form" (ngSubmit)="guardar()" novalidate>
          <div class="campo">
            <label for="p-email">Correo electronico</label>
            <input id="p-email" type="email" formControlName="email" autocomplete="email" />
            @if (form.controls.email.touched && form.controls.email.invalid) {
              <p class="error-campo">Ingresa un correo electronico valido.</p>
            }
          </div>

          <div class="campo">
            <label for="p-telefono">Telefono</label>
            <input id="p-telefono" type="tel" formControlName="telefono" autocomplete="tel" />
            @if (form.controls.telefono.touched && form.controls.telefono.invalid) {
              <p class="error-campo">
                Ingresa un telefono valido (entre 7 y 20 caracteres, numeros, espacios, parentesis o guiones).
              </p>
            }
          </div>

          @if (error()) {
            <div class="aviso error">{{ error() }}</div>
          }

          @if (aviso()) {
            <div class="aviso exito">{{ aviso() }}</div>
          }

          <button type="submit" [disabled]="guardando()">
            {{ guardando() ? 'Guardando...' : 'Guardar cambios' }}
          </button>
        </form>
      }
    </section>
  `,
  styles: [
    `
      .intro {
        color: #5b5470;
      }

      .panel {
        max-width: 560px;
      }

      .campo {
        margin-bottom: 0.9rem;
      }

      form button[type='submit'] {
        margin-top: 0.4rem;
      }
    `,
  ],
})
export class PerfilComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly aviso = signal('');

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.required, Validators.pattern(/^\+?[0-9 ()-]{7,20}$/)]],
  });

  ngOnInit(): void {
    this.auth.me().subscribe({
      next: (usuario) => {
        this.form.setValue({
          email: usuario.email ?? '',
          telefono: usuario.telefono ?? '',
        });
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus datos.');
        this.cargando.set(false);
      },
    });
  }

  guardar(): void {
    this.error.set('');
    this.aviso.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const datos = this.form.getRawValue();

    this.auth.actualizarDatos(datos).subscribe({
      next: () => {
        this.aviso.set('Tus datos se actualizaron correctamente.');
        this.form.markAsPristine();
      },
      error: () => {
        this.error.set('No se pudieron guardar tus datos. Verifica el correo y el telefono.');
      },
      complete: () => this.guardando.set(false),
    });
  }
}
