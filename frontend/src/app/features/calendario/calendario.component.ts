import { CommonModule, formatDate } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CalendarioService } from '../../core/calendario.service';
import {
  CATEGORIAS,
  EventoCalendario,
  FiltrosCalendario,
  etiquetaCategoria,
} from '../../core/models';

@Component({
  selector: 'app-calendario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Calendario de eventos</h1>
    <p class="intro">
      Vista cronologica de las carreras de las cuatro categorias FIA. Los filtros se aplican de
      inmediato y las descargas respetan lo seleccionado en pantalla.
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
          <label for="f-anio">Año</label>
          <select id="f-anio" [(ngModel)]="filtros.anio" (ngModelChange)="cargar()">
            <option [ngValue]="null">Todos</option>
            @for (anio of anios; track anio) {
              <option [ngValue]="anio">{{ anio }}</option>
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

        <div>
          <label for="f-circuito">Circuito</label>
          <input
            id="f-circuito"
            type="text"
            placeholder="Nombre del circuito"
            [(ngModel)]="filtros.circuito"
            (ngModelChange)="cargar()"
          />
        </div>

        <div class="acciones">
          <button type="button" class="secundario" (click)="limpiarFiltros()">Limpiar</button>
        </div>
      </div>

      <div class="descargas">
        <button type="button" (click)="descargar('pdf')" [disabled]="descargando() !== null">
          {{ descargando() === 'pdf' ? 'Generando PDF...' : 'Descargar PDF' }}
        </button>
        <button type="button" (click)="descargar('ics')" [disabled]="descargando() !== null">
          {{ descargando() === 'ics' ? 'Generando ICS...' : 'Descargar ICS' }}
        </button>
      </div>

      @if (error()) {
        <div class="aviso error">{{ error() }}</div>
      }
    </section>

    @if (cargando()) {
      <p class="cargando">Cargando eventos...</p>
    } @else if (eventosOrdenados().length === 0) {
      <p class="vacio">No hay carreras definidas para los filtros seleccionados.</p>
    } @else {
      <section class="grupo">
        <table>
          <thead>
            <tr>
              <th>Fechas</th>
              <th>Categoria</th>
              <th>Evento</th>
              <th>Tipo</th>
              <th>Circuito</th>
              <th>Pais</th>
            </tr>
          </thead>
          <tbody>
            @for (evento of eventosOrdenados(); track evento.id) {
              <tr>
                <td>{{ rangoFechas(evento) }}</td>
                <td><strong>{{ etiqueta(evento) }}</strong></td>
                <td>{{ evento.nombre || 'Evento' }}</td>
                <td>{{ evento.tipo || '-' }}</td>
                <td>{{ evento.circuito }}</td>
                <td>{{ evento.pais || '-' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </section>
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
        align-items: flex-end;
      }

      .descargas {
        display: flex;
        gap: 0.6rem;
        margin-top: 0.9rem;
      }

      .grupo {
        margin-bottom: 1.5rem;
      }

      table {
        box-shadow: 0 2px 10px rgba(43, 16, 85, 0.08);
        border-radius: 8px;
        overflow: hidden;
      }
    `,
  ],
})
export class CalendarioComponent implements OnInit {
  private readonly calendario = inject(CalendarioService);

  readonly categorias = CATEGORIAS;
  readonly anios = [2026, 2025, 2024];
  readonly eventos = signal<EventoCalendario[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly descargando = signal<'pdf' | 'ics' | null>(null);

  filtros: Required<FiltrosCalendario> = {
    categoria: '',
    anio: null,
    desde: '',
    hasta: '',
    circuito: '',
  };

  readonly eventosOrdenados = computed(() =>
    [...this.eventos()].sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio)),
  );

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.calendario.listar(this.filtrosActuales()).subscribe({
      next: (eventos) => this.eventos.set(eventos ?? []),
      error: () => {
        this.error.set('No se pudo cargar el calendario.');
        this.eventos.set([]);
        this.cargando.set(false);
      },
      complete: () => this.cargando.set(false),
    });
  }

  limpiarFiltros(): void {
    this.filtros = { categoria: '', anio: null, desde: '', hasta: '', circuito: '' };
    this.cargar();
  }

  rangoFechas(evento: EventoCalendario): string {
    const formatear = (valor?: string | null) =>
      valor ? formatDate(valor, 'dd/MM/yyyy', 'en-GB') : '';
    const inicio = formatear(evento.fechaInicio);
    const fin = formatear(evento.fechaFin);
    if (!inicio) {
      return '-';
    }
    return fin && fin !== inicio ? `${inicio} - ${fin}` : inicio;
  }

  etiqueta(evento: EventoCalendario): string {
    return etiquetaCategoria(evento.categoria);
  }

  descargar(formato: 'pdf' | 'ics'): void {
    this.error.set('');
    this.descargando.set(formato);

    const peticion =
      formato === 'pdf'
        ? this.calendario.descargarPdf(this.filtrosActuales())
        : this.calendario.descargarIcs(this.filtrosActuales());

    peticion.subscribe({
      next: (blob) => this.guardarArchivo(blob, formato === 'pdf' ? 'calendario-fia.pdf' : 'calendario-fia.ics'),
      error: () => this.error.set('No se pudo generar la descarga.'),
      complete: () => this.descargando.set(null),
    });
  }

  private filtrosActuales(): FiltrosCalendario {
    return {
      categoria: this.filtros.categoria || undefined,
      anio: this.filtros.anio ?? undefined,
      desde: this.filtros.desde || undefined,
      hasta: this.filtros.hasta || undefined,
      circuito: this.filtros.circuito.trim() || undefined,
    };
  }

  private guardarArchivo(blob: Blob, nombre: string): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
    URL.revokeObjectURL(url);
  }
}
