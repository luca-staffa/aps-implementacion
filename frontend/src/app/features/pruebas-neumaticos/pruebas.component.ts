import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { PruebaNeumatico, ResultadoPrueba } from '../../core/models';
import { PruebasService } from '../../core/pruebas.service';

interface GrupoEscuderia {
  escuderia: string;
  resultados: ResultadoPrueba[];
}

@Component({
  selector: 'app-pruebas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Pruebas de neumaticos</h1>
    <p class="intro">
      Informacion anual de las pruebas de neumaticos: las dos fechas de la prueba, el circuito, las
      categorias participantes y los resultados por escuderia. Vista de solo lectura.
    </p>

    <section class="tarjeta filtros">
      <div class="grilla-filtros">
        <div>
          <label for="f-anio">Anio</label>
          <select id="f-anio" [(ngModel)]="anio" (ngModelChange)="cargar()">
            @for (opcion of anios; track opcion) {
              <option [value]="opcion">{{ opcion }}</option>
            }
          </select>
        </div>
      </div>

      <div class="aviso info">
        Los resultados son de caracter informativo y no se pueden modificar desde esta vista.
      </div>

      @if (error()) {
        <div class="aviso error">{{ error() }}</div>
      }
    </section>

    @if (cargando()) {
      <p class="cargando">Cargando pruebas...</p>
    } @else if (pruebas().length === 0) {
      <p class="vacio">No hay pruebas de neumaticos registradas para el anio seleccionado.</p>
    } @else {
      @for (prueba of pruebas(); track prueba.id) {
        <section class="tarjeta prueba">
          <header class="cabecera">
            <div>
              <h2>{{ prueba.circuito }}</h2>
              <p class="anio">Anio {{ prueba.anio }}</p>
            </div>
            <div class="fechas">
              <div>
                <span class="etiqueta">Desde</span>
                <span class="valor">{{ prueba.fechaInicio | date: 'dd/MM/yyyy' }}</span>
              </div>
              <div>
                <span class="etiqueta">Hasta</span>
                <span class="valor">{{ prueba.fechaFin ? (prueba.fechaFin | date: 'dd/MM/yyyy') : '-' }}</span>
              </div>
            </div>
          </header>

          <div class="categorias">
            <span class="etiqueta">Categorias:</span>
            @for (categoria of categorias(prueba); track categoria) {
              <span class="chip">{{ categoria }}</span>
            }
          </div>

          @if (prueba.avisoPublicado) {
            <div class="aviso atencion">Prueba anunciada al inicio del anio.</div>
          }

          <h3>Resultados por escuderia</h3>
          @if (grupos(prueba).length === 0) {
            <p class="vacio">Sin resultados cargados.</p>
          } @else {
            @for (grupo of grupos(prueba); track grupo.escuderia) {
              <div class="escuderia">
                <h4>{{ grupo.escuderia }}</h4>
                <table>
                  <thead>
                    <tr>
                      <th>Piloto</th>
                      <th>Posicion</th>
                      <th>Mejor tiempo</th>
                      <th>Vueltas</th>
                    </tr>
                  </thead>
                  <tbody>
                    @for (resultado of grupo.resultados; track $index) {
                      <tr>
                        <td>{{ resultado.piloto || '-' }}</td>
                        <td>{{ resultado.posicion ?? '-' }}</td>
                        <td>{{ resultado.mejorTiempo || '-' }}</td>
                        <td>{{ resultado.vueltas ?? '-' }}</td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            }
          }
        </section>
      }
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

      .prueba {
        margin-bottom: 1.5rem;
      }

      .cabecera {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        gap: 1rem;
        border-bottom: 1px solid #e2ddf0;
        padding-bottom: 0.75rem;
      }

      .cabecera h2 {
        margin-bottom: 0.2rem;
      }

      .anio {
        margin: 0;
        color: #5b5470;
        font-size: 0.85rem;
      }

      .fechas {
        display: flex;
        gap: 1.5rem;
      }

      .fechas .etiqueta {
        display: block;
      }

      .fechas .valor {
        font-weight: 600;
        color: #2b1055;
      }

      .etiqueta {
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0.03em;
        color: #5b5470;
      }

      .categorias {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 0.4rem;
        margin: 0.9rem 0;
      }

      .chip {
        background: #f3effb;
        color: #4b2a8a;
        border-radius: 999px;
        padding: 0.15rem 0.7rem;
        font-size: 0.8rem;
      }

      .escuderia {
        margin-top: 1rem;
      }

      .escuderia h4 {
        margin: 0 0 0.4rem;
        color: #4b2a8a;
      }

      table {
        border: 1px solid #e2ddf0;
        border-radius: 8px;
        overflow: hidden;
      }
    `,
  ],
})
export class PruebasComponent implements OnInit {
  private readonly servicio = inject(PruebasService);

  readonly anios: number[] = this.construirAnios();
  anio = new Date().getFullYear();

  readonly pruebas = signal<PruebaNeumatico[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.servicio.listar(Number(this.anio)).subscribe({
      next: (pruebas) => this.pruebas.set(pruebas ?? []),
      error: () => {
        this.error.set('No se pudieron cargar las pruebas de neumaticos.');
        this.pruebas.set([]);
        this.cargando.set(false);
      },
      complete: () => this.cargando.set(false),
    });
  }

  categorias(prueba: PruebaNeumatico): string[] {
    return (prueba.categorias ?? '')
      .split(',')
      .map((categoria) => categoria.trim())
      .filter((categoria) => categoria.length > 0);
  }

  grupos(prueba: PruebaNeumatico): GrupoEscuderia[] {
    const mapa = new Map<string, ResultadoPrueba[]>();
    for (const resultado of prueba.resultados ?? []) {
      const clave = resultado.escuderia || 'Sin escuderia';
      const lista = mapa.get(clave) ?? [];
      lista.push(resultado);
      mapa.set(clave, lista);
    }
    return Array.from(mapa.entries())
      .map(([escuderia, resultados]) => ({ escuderia, resultados }))
      .sort((a, b) => a.escuderia.localeCompare(b.escuderia));
  }

  private construirAnios(): number[] {
    const actual = new Date().getFullYear();
    const anios: number[] = [];
    for (let i = 0; i < 6; i++) {
      anios.push(actual - i);
    }
    return anios;
  }
}