import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

/** Capa delgada sobre HttpClient que resuelve la URL base de la API. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  get<T>(ruta: string, params?: HttpParams): Observable<T> {
    return this.http.get<T>(`${this.base}${ruta}`, { params });
  }

  getBlob(ruta: string, params?: HttpParams): Observable<Blob> {
    return this.http.get(`${this.base}${ruta}`, { params, responseType: 'blob' });
  }

  post<T>(ruta: string, cuerpo: unknown): Observable<T> {
    return this.http.post<T>(`${this.base}${ruta}`, cuerpo);
  }

  put<T>(ruta: string, cuerpo: unknown): Observable<T> {
    return this.http.put<T>(`${this.base}${ruta}`, cuerpo);
  }

  delete<T>(ruta: string): Observable<T> {
    return this.http.delete<T>(`${this.base}${ruta}`);
  }
}
