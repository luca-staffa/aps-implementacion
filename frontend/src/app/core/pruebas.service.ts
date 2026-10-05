import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { PruebaNeumatico } from './models';

@Injectable({ providedIn: 'root' })
export class PruebasService {
  private readonly api = inject(ApiService);

  listar(anio?: number): Observable<PruebaNeumatico[]> {
    let params = new HttpParams();
    if (anio) {
      params = params.set('anio', anio.toString());
    }
    return this.api.get<PruebaNeumatico[]>('/pruebas-neumaticos', params);
  }

  obtener(id: number): Observable<PruebaNeumatico> {
    return this.api.get<PruebaNeumatico>(`/pruebas-neumaticos/${id}`);
  }
}
