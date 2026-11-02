import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { Escuderia } from './models';

@Injectable({ providedIn: 'root' })
export class EscuderiasService {
  private readonly api = inject(ApiService);

  listar(): Observable<Escuderia[]> {
    return this.api.get<Escuderia[]>('/escuderias');
  }

  actualizar(id: number, datos: { nombre: string; pais?: string | null }): Observable<Escuderia> {
    return this.api.put<Escuderia>(`/escuderias/${id}`, datos);
  }
}
