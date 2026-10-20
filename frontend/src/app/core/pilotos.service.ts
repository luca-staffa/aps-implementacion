import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';

export type RolPiloto = 'TITULAR' | 'SUPLENTE';

export interface Piloto {
  id?: number;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  nacionalidad: string;
  numero: number;
  abreviatura: string;
  rol: RolPiloto;
  numeroLicenciaFia: string;
  fotoUrl?: string | null;
  activo: boolean;
  categoriaId: number;
  categoriaCodigo?: string;
  temporadaId: number;
  temporadaAnio?: number;
  escuderiaId?: number | null;
  escuderiaNombre?: string | null;
}

export interface PilotoPublico {
  id?: number;
  nombre: string;
  apellido: string;
  nacionalidad: string;
  numero: number;
  abreviatura: string;
  rol: RolPiloto;
  fotoUrl?: string | null;
  categoriaId?: number | null;
  categoriaCodigo?: string | null;
  temporadaId?: number | null;
  temporadaAnio?: number | null;
  escuderiaId?: number | null;
  escuderiaNombre?: string | null;
}

export interface PilotoPayload {
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  nacionalidad: string;
  numero: number;
  abreviatura: string;
  rol: RolPiloto;
  numeroLicenciaFia: string;
  fotoUrl?: string | null;
  categoriaId: number;
  temporadaId: number;
  escuderiaId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class PilotosService {
  private readonly api = inject(ApiService);

  listar(filtros: { categoriaId?: number; categoria?: string; temporadaId?: number; temporada?: number; escuderiaId?: number } = {}): Observable<Piloto[]> {
    let params = new HttpParams();
    if (filtros.categoriaId) {
      params = params.set('categoriaId', String(filtros.categoriaId));
    }
    if (filtros.categoria) {
      params = params.set('categoria', filtros.categoria);
    }
    if (filtros.temporadaId) {
      params = params.set('temporadaId', String(filtros.temporadaId));
    }
    if (filtros.temporada) {
      params = params.set('temporada', String(filtros.temporada));
    }
    if (filtros.escuderiaId) {
      params = params.set('escuderiaId', String(filtros.escuderiaId));
    }
    return this.api.get<Piloto[]>('/pilotos', params);
  }

  listarPublico(filtros: { categoriaId?: number; temporadaId?: number; escuderiaId?: number; escuderia?: string } = {}): Observable<PilotoPublico[]> {
    let params = new HttpParams();
    if (filtros.categoriaId) {
      params = params.set('categoriaId', String(filtros.categoriaId));
    }
    if (filtros.temporadaId) {
      params = params.set('temporadaId', String(filtros.temporadaId));
    }
    if (filtros.escuderiaId) {
      params = params.set('escuderiaId', String(filtros.escuderiaId));
    }
    if (filtros.escuderia && filtros.escuderia.trim()) {
      params = params.set('escuderia', filtros.escuderia.trim());
    }
    return this.api.get<PilotoPublico[]>('/pilotos/publico', params);
  }

  crear(payload: PilotoPayload): Observable<Piloto> {
    return this.api.post<Piloto>('/pilotos', payload);
  }

  actualizar(id: number, payload: PilotoPayload): Observable<Piloto> {
    return this.api.put<Piloto>(`/pilotos/${id}`, payload);
  }

  eliminar(id: number): Observable<void> {
    return this.api.delete<void>(`/pilotos/${id}`);
  }
}
