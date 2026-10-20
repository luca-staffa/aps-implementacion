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
}
