import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="contenedor">
      <section class="tarjeta panel">
        <h1>FIA</h1>
        <p class="subtitulo">Comision Violeta &middot; Acceso al sistema</p>

        <form [formGroup]="form" (ngSubmit)="enviar()" novalidate>
          <div class="campo">
            <label for="username">Usuario</label>
            <input id="username" type="text" formControlName="username" autocomplete="username" />
            @if (form.controls.username.touched && form.controls.username.invalid) {
              <p class="error-campo">Ingresa tu usuario.</p>
            }
          </div>

          <div class="campo">
            <label for="password">Contrasena</label>
            <input
              id="password"
              type="password"
              formControlName="password"
              autocomplete="current-password"
            />
            @if (form.controls.password.touched && form.controls.password.invalid) {
              <p class="error-campo">Ingresa tu contrasena.</p>
            }
          </div>

          @if (error) {
            <div class="aviso error">{{ error }}</div>
          }

          <button type="submit" [disabled]="cargando">
            {{ cargando ? 'Ingresando...' : 'Ingresar' }}
          </button>
        </form>

        <div class="nota">
          <p class="titulo-nota">Perfiles disponibles</p>
          <ul>
            <li><strong>ADMIN_FIA:</strong> vista consolidada, alta y edicion de sanciones, pilotos de todas las escuderias.</li>
            <li><strong>ADMIN_ESCUDERIA:</strong> gestion de los pilotos de su escuderia.</li>
            <li><strong>PUBLICO:</strong> consulta de calendario, pilotos publicos y pruebas de neumaticos.</li>
          </ul>
          <p>El calendario, los pilotos publicos y las pruebas pueden consultarse sin iniciar sesion.</p>
        </div>
      </section>
    </div>
  `,
  styles: [
    `
      .contenedor {
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 1.5rem;
      }

      .panel {
        width: 100%;
        max-width: 420px;
      }

      h1 {
        margin-bottom: 0.2rem;
      }

      .subtitulo {
        margin-top: 0;
        color: #5b5470;
        font-size: 0.9rem;
      }

      .campo {
        margin-bottom: 0.9rem;
      }

      form button[type='submit'] {
        width: 100%;
        margin-top: 0.4rem;
      }

      .nota {
        margin-top: 1.4rem;
        border-top: 1px solid #e2ddf0;
        padding-top: 0.9rem;
        font-size: 0.82rem;
        color: #5b5470;
      }

      .nota ul {
        margin: 0.4rem 0 0;
        padding-left: 1.1rem;
      }

      .titulo-nota {
        margin: 0;
        font-weight: 600;
        color: #2b1055;
      }
    `,
  ],
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);

  cargando = false;
  error = '';

  readonly form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  ngOnInit(): void {
    if (this.auth.autenticado()) {
      this.router.navigate(['/dashboard']);
    }
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando = true;
    this.error = '';
    const { username, password } = this.form.getRawValue();

    this.auth.login(username, password).subscribe({
      next: () => {
        const destino = this.ruta.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
        this.router.navigateByUrl(destino);
      },
      error: () => {
        this.error = 'Usuario o contrasena incorrectos.';
        this.cargando = false;
      },
      complete: () => {
        this.cargando = false;
      },
    });
  }
}
