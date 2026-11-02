import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { MiembroEscuderia, RolEscuderia } from './models';

@Injectable({ providedIn: 'root' })
export class EquipoService {
  private readonly api = inject(ApiService);

  listarMiembros(): Observable<MiembroEscuderia[]> {
    return this.api.get<MiembroEscuderia[]>('/escuderias/miembros');
  }

  asignarRol(usuarioId: number, rolEscuderia: RolEscuderia): Observable<MiembroEscuderia> {
    return this.api.put<MiembroEscuderia>(`/escuderias/miembros/${usuarioId}`, { rolEscuderia });
  }

  revocarRol(usuarioId: number): Observable<MiembroEscuderia> {
    return this.api.delete<MiembroEscuderia>(`/escuderias/miembros/${usuarioId}/rol`);
  }
}
