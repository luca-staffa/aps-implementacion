import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { ControlTecnico, ControlTecnicoPayload } from './models';

@Injectable({ providedIn: 'root' })
export class ControlesService {
  private readonly api = inject(ApiService);

  listar(escuderiaId?: number): Observable<ControlTecnico[]> {
    let params = new HttpParams();
    if (escuderiaId) {
      params = params.set('escuderiaId', String(escuderiaId));
    }
    return this.api.get<ControlTecnico[]>('/controles-tecnicos', params);
  }

  obtener(id: number): Observable<ControlTecnico> {
    return this.api.get<ControlTecnico>(`/controles-tecnicos/${id}`);
  }

  crear(payload: ControlTecnicoPayload): Observable<ControlTecnico> {
    return this.api.post<ControlTecnico>('/controles-tecnicos', payload);
  }

  actualizar(id: number, payload: ControlTecnicoPayload): Observable<ControlTecnico> {
    return this.api.put<ControlTecnico>(`/controles-tecnicos/${id}`, payload);
  }
}
