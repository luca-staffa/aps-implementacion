import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { Usuario } from './models';

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly api = inject(ApiService);

  listar(): Observable<Usuario[]> {
    return this.api.get<Usuario[]>('/usuarios');
  }

  obtener(id: number): Observable<Usuario> {
    return this.api.get<Usuario>(`/usuarios/${id}`);
  }
}
