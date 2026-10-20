import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CATEGORIAS, TEMPORADAS } from '../../core/models';
import {
  DashboardCategoria,
  DashboardConsolidadoResponse,
  DashboardConsolidadoService,
} from '../../core/dashboard-consolidado.service';

@Component({
  selector: 'app-dashboard-consolidado',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Vista consolidada</h1>
    <p class="intro">
      Resumen operativo de la FIA por categoria y temporada, orientado a la toma de decisiones del
      equipo administrativo.
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
          <label for="f-temporada">Temporada</label>
          <select id="f-temporada" [(ngModel)]="filtros.temporada" (ngModelChange)="cargar()">
            <option [ngValue]="null">Todas las temporadas</option>
            @for (temporada of temporadas; track temporada) {
              <option [ngValue]="temporada">{{ temporada }}</option>
            }
          </select>
        </div>

        <div class="acciones">
          <button type="button" class="secundario" (click)="limpiarFiltros()">Limpiar</button>
        </div>
      </div>
    </section>

    @if (cargando()) {
      <p class="cargando">Cargando datos del dashboard...</p>
    } @else if (error()) {
      <div class="aviso error">{{ error() }}</div>
    } @else if (datos().categorias.length === 0) {
      <p class="vacio">No hay datos para los filtros seleccionados.</p>
    } @else {
      @for (categoria of datos().categorias; track categoria.categoriaId) {
        <section class="grupo tarjeta">
          <div class="encabezado">
            <div>
              <span class="etiqueta">{{ categoria.codigo }}</span>
              <h2>{{ categoria.nombre }}</h2>
            </div>
            <div class="temporada">{{ etiquetaTemporada(categoria.temporada) }}</div>
          </div>

          <div class="kpis">
            <div class="kpi">
              <span>Eventos</span>
              <strong>{{ categoria.eventosCalendario }}</strong>
            </div>
            <div class="kpi">
              <span>Sanciones</span>
              <strong>{{ categoria.sanciones }}</strong>
            </div>
            <div class="kpi">
              <span>Pruebas</span>
              <strong>{{ categoria.pruebasNeumaticos }}</strong>
            </div>
            <div class="kpi">
              <span>Pilotos</span>
              <strong>{{ categoria.pilotos }}</strong>
            </div>
          </div>

          @if (categoria.atributos.length > 0) {
            <div class="atributos">
              <h3>Atributos clave</h3>
              <ul>
                @for (atributo of categoria.atributos; track atributo.clave) {
                  <li>
                    <strong>{{ atributo.clave }}</strong>
                    <span>{{ atributo.valor }}</span>
                    @if (atributo.descripcion) {
                      <small>{{ atributo.descripcion }}</small>
                    }
                  </li>
                }
              </ul>
            </div>
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

      .grilla-filtros {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
        gap: 1rem;
        align-items: end;
      }

      .acciones {
        display: flex;
        align-items: flex-end;
      }

      .grupo {
        margin-top: 1.25rem;
      }

      .encabezado {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
        margin-bottom: 1rem;
        flex-wrap: wrap;
      }

      .etiqueta {
        display: inline-block;
        background: #efe9ff;
        color: #3d2b6d;
        border-radius: 999px;
        font-weight: 700;
        padding: 0.25rem 0.6rem;
        font-size: 0.8rem;
        margin-bottom: 0.25rem;
      }

      .temporada {
        font-size: 0.85rem;
        color: #5b5470;
      }

      .kpis {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
        gap: 0.75rem;
        margin-bottom: 1rem;
      }

      .kpi {
        background: #f8f5ff;
        border: 1px solid #e3dafd;
        border-radius: 10px;
        padding: 0.8rem 0.9rem;
      }

      .kpi span {
        display: block;
        color: #5b5470;
        font-size: 0.8rem;
      }

      .kpi strong {
        display: block;
        margin-top: 0.35rem;
        font-size: 1.4rem;
      }

      .atributos ul {
        list-style: none;
        margin: 0;
        padding: 0;
        display: grid;
        gap: 0.7rem;
      }

      .atributos li {
        background: #faf8ff;
        border: 1px solid #e9e1ff;
        border-radius: 8px;
        padding: 0.75rem 0.8rem;
        display: flex;
        flex-direction: column;
        gap: 0.2rem;
      }

      .atributos small {
        color: #5b5470;
      }
    `,
  ],
})
export class DashboardConsolidadoComponent {
  private readonly dashboard = inject(DashboardConsolidadoService);

  readonly categorias = CATEGORIAS;
  readonly temporadas = TEMPORADAS;
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly datos = signal<DashboardConsolidadoResponse>({ categorias: [] });

  filtros: { categoria: string; temporada: number | null } = {
    categoria: '',
    temporada: null,
  };

  readonly resumen = computed<DashboardCategoria[]>(() => this.datos().categorias ?? []);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.dashboard
      .listar({
        categoria: this.filtros.categoria || undefined,
        temporada: this.filtros.temporada,
      })
      .subscribe({
        next: (respuesta) => this.datos.set(respuesta),
        error: () => {
          this.error.set('No se pudo cargar la vista consolidada.');
          this.datos.set({ categorias: [] });
        },
        complete: () => this.cargando.set(false),
      });
  }

  limpiarFiltros(): void {
    this.filtros = { categoria: '', temporada: null };
    this.cargar();
  }

  etiquetaTemporada(temporada: number | null): string {
    return temporada == null ? 'Todas las temporadas' : `Temporada ${temporada}`;
  }
}
