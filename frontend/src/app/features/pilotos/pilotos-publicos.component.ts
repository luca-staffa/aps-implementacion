import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CATEGORIAS } from '../../core/models';
import { PilotoPublico, PilotosService } from '../../core/pilotos.service';

@Component({
  selector: 'app-pilotos-publicos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Pilotos</h1>
    <p class="intro">Consulta publicamente los pilotos activos por categoria, temporada y escudería.</p>

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

        <div>
          <label for="f-escuderia">Escuderia</label>
          <input id="f-escuderia" [(ngModel)]="filtroEscuderia" (ngModelChange)="cargar()" />
        </div>
      </div>
    </section>

    @if (cargando()) {
      <p class="cargando">Cargando pilotos...</p>
    } @else if (pilotos().length === 0) {
      <p class="vacio">No hay pilotos para los filtros actuales.</p>
    } @else {
      <div class="lista">
        @for (piloto of pilotos(); track piloto.id) {
          <article class="tarjeta piloto">
            <div class="encabezado">
              <div>
                <span class="numero">#{{ piloto.numero }}</span>
                <h2>{{ piloto.nombre }} {{ piloto.apellido }}</h2>
              </div>
              <span class="rol">{{ piloto.rol }}</span>
            </div>
            <div class="meta">
              <span>{{ piloto.categoriaCodigo || 'Categoria' }}</span>
              <span>{{ piloto.escuderiaNombre || 'Escuderia' }}</span>
              <span>Temporada {{ piloto.temporadaAnio || '-' }}</span>
            </div>
            <p>{{ piloto.nacionalidad }} · Abreviatura {{ piloto.abreviatura }}</p>
          </article>
        }
      </div>
    }
  `,
  styles: [
    `
      .intro { color: #5b5470; }
      .grilla-filtros { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; align-items: end; }
      .lista { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; margin-top: 1rem; }
      .piloto { padding: 1rem; }
      .encabezado { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; }
      .numero { font-weight: 700; color: #4c2e8a; }
      .rol { background: #efe9ff; color: #3d2b6d; border-radius: 999px; padding: 0.25rem 0.6rem; font-size: 0.75rem; }
      .meta { display: flex; gap: 0.5rem; flex-wrap: wrap; font-size: 0.8rem; color: #5b5470; }
      input, select { padding: 0.55rem 0.7rem; border: 1px solid #d9d2ee; border-radius: 8px; }
    `,
  ],
})
export class PilotosPublicosComponent {
  private readonly servicio = inject(PilotosService);

  readonly categorias = CATEGORIAS;
  readonly temporadas = [
    { id: 1, anio: 2026 },
    { id: 2, anio: 2025 },
    { id: 3, anio: 2024 },
  ];
  readonly categoriaIds: Record<string, number> = { F1: 1, F2: 2, F3: 3, F1_ACADEMY: 4 };
  readonly pilotos = signal<PilotoPublico[]>([]);
  readonly cargando = signal(false);

  filtroCategoria = '';
  filtroTemporada = '';
  filtroEscuderia = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    const categoriaId = this.filtroCategoria ? Number(this.filtroCategoria) : undefined;
    const temporadaId = this.filtroTemporada ? Number(this.filtroTemporada) : undefined;
    const escuderiaId = this.filtroEscuderia ? Number(this.filtroEscuderia) : undefined;

    this.cargando.set(true);
    this.servicio.listarPublico({ categoriaId, temporadaId, escuderiaId }).subscribe({
      next: (respuesta) => this.pilotos.set(respuesta ?? []),
      error: () => this.pilotos.set([]),
      complete: () => this.cargando.set(false),
    });
  }
}
