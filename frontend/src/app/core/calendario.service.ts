import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { EventoCalendario, FiltrosCalendario } from './models';

@Injectable({ providedIn: 'root' })
export class CalendarioService {
  private readonly api = inject(ApiService);

  listar(filtros: FiltrosCalendario = {}): Observable<EventoCalendario[]> {
    return this.api.get<EventoCalendario[]>('/calendario/eventos', this.aParametros(filtros));
  }

  obtener(id: number): Observable<EventoCalendario> {
    return this.api.get<EventoCalendario>(`/calendario/eventos/${id}`);
  }

  descargarPdf(filtros: FiltrosCalendario = {}): Observable<Blob> {
    return this.api.getBlob('/calendario/eventos/export.pdf', this.aParametros(filtros));
  }

  descargarIcs(filtros: FiltrosCalendario = {}): Observable<Blob> {
    return this.api.getBlob('/calendario/eventos/export.ics', this.aParametros(filtros));
  }

  private aParametros(filtros: FiltrosCalendario): HttpParams {
    let params = new HttpParams();
    if (filtros.categoria) {
      params = params.set('categoria', filtros.categoria);
    }
    if (filtros.anio) {
      params = params.set('anio', String(filtros.anio));
    }
    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    if (filtros.circuito) {
      params = params.set('circuito', filtros.circuito);
    }
    return params;
  }
}
