import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';

export interface DashboardConsolidadoResponse {
  categorias: DashboardCategoria[];
}

export interface DashboardCategoria {
  categoriaId: number;
  codigo: string;
  nombre: string;
  temporadaActual: number;
  eventosCalendario: number;
  sanciones: number;
  pruebasNeumaticos: number;
  atributos: DashboardAtributo[];
}

export interface DashboardAtributo {
  clave: string;
  valor: string;
  descripcion?: string | null;
}

@Injectable({ providedIn: 'root' })
export class DashboardConsolidadoService {
  private readonly api = inject(ApiService);

  listar(filtros: { categoria?: string; temporada?: number | string } = {}): Observable<DashboardConsolidadoResponse> {
    return this.api.get<DashboardConsolidadoResponse>('/dashboard/consolidado', this.aParametros(filtros));
  }

  private aParametros(filtros: { categoria?: string; temporada?: number | string }): HttpParams {
    let params = new HttpParams();
    if (filtros.categoria) {
      params = params.set('categoria', filtros.categoria);
    }
    if (filtros.temporada) {
      params = params.set('temporada', String(filtros.temporada));
    }
    return params;
  }
}
