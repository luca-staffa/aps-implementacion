import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { FiltrosSanciones, Sancion, SancionPayload, VistoDetalle } from './models';

@Injectable({ providedIn: 'root' })
export class SancionesService {
  private readonly api = inject(ApiService);

  listar(filtros: FiltrosSanciones = {}): Observable<Sancion[]> {
    return this.api.get<Sancion[]>('/sanciones', this.aParametros(filtros));
  }

  obtener(id: number): Observable<Sancion> {
    return this.api.get<Sancion>(`/sanciones/${id}`);
  }

  crear(payload: SancionPayload): Observable<Sancion> {
    return this.api.post<Sancion>('/sanciones', payload);
  }

  actualizar(id: number, payload: SancionPayload): Observable<Sancion> {
    return this.api.put<Sancion>(`/sanciones/${id}`, payload);
  }

  eliminar(id: number): Observable<void> {
    return this.api.delete<void>(`/sanciones/${id}`);
  }

  marcarVisto(id: number): Observable<VistoDetalle> {
    return this.api.post<VistoDetalle>(`/sanciones/${id}/visto`, {});
  }

  private aParametros(filtros: FiltrosSanciones): HttpParams {
    let params = new HttpParams();
    if (filtros.categoria) {
      params = params.set('categoria', filtros.categoria);
    }
    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    return params;
  }
}
