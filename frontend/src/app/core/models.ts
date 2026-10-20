export type Rol = 'ADMIN_FIA' | 'ADMIN_ESCUDERIA' | 'PUBLICO';

export interface Usuario {
  id?: number;
  username: string;
  email?: string;
  telefono?: string | null;
  nombre: string;
  apellido?: string;
  rol: Rol;
  activo?: boolean;
}

export interface Sesion {
  token: string;
  tipo: string;
  username: string;
  rol: Rol;
  /** Duracion de la sesion en milisegundos (tal como la envia el backend). */
  expiraEn: number;
  /** Marca de tiempo (ms) en la que la sesion deja de ser valida. */
  expiraEnAbsoluto?: number;
}

export interface Credenciales {
  username: string;
  password: string;
}

export interface RegistroDatos {
  username: string;
  email: string;
  password: string;
  nombre: string;
}

export interface VistoDetalle {
  sancionId: number;
  usuarioId: number;
  username: string;
  fechaVisto: string;
  visto: boolean;
}

export interface Sancion {
  id: number;
  pilotoId?: number | null;
  pilotoNombre?: string | null;
  escuderiaId?: number | null;
  escuderiaNombre?: string | null;
  categoria: string;
  eventoId?: number | null;
  eventoNombre?: string | null;
  eventoCircuito?: string | null;
  motivo: string;
  fecha: string;
  tipo: string;
  descripcion?: string | null;
  cantidadVistas: number;
  vistas: VistoDetalle[];
}

export interface SancionPayload {
  categoria: string;
  eventoId: number | null;
  pilotoId: number | null;
  escuderiaId: number | null;
  motivo: string;
  fecha: string;
  tipo: string;
  descripcion?: string | null;
}

export interface EventoCalendario {
  id: number;
  categoria: string;
  nombre?: string;
  circuito: string;
  pais?: string;
  fechaInicio: string;
  fechaFin?: string | null;
  tipo?: string | null;
}

export interface ResultadoPrueba {
  id: number;
  pruebaId?: number | null;
  escuderia?: string | null;
  piloto?: string | null;
  mejorTiempo?: string | null;
  vueltas?: number | null;
  posicion?: number | null;
}

export interface PruebaNeumatico {
  id: number;
  anio: number;
  circuito: string;
  pais?: string | null;
  fechaInicio: string;
  fechaFin?: string | null;
  /** Categorias separadas por coma tal como las envia el backend (p. ej. "F1,F2"). */
  categorias: string;
  avisoPublicado: boolean;
  resultados: ResultadoPrueba[];
}

export interface FiltrosCalendario {
  categoria?: string;
  anio?: number | null;
  desde?: string;
  hasta?: string;
  circuito?: string;
}

export interface FiltrosSanciones {
  categoria?: string;
  pilotoId?: number | null;
  escuderiaId?: number | null;
  eventoId?: number | null;
  desde?: string;
  hasta?: string;
}

export interface Escuderia {
  id: number;
  nombre: string;
  pais?: string | null;
  activa?: boolean;
}

export interface Categoria {
  valor: string;
  etiqueta: string;
  alias: string[];
}

/** Las cuatro categorias agrupadas del calendario FIA. */
export const CATEGORIAS: Categoria[] = [
  { valor: 'F1', etiqueta: 'Formula 1', alias: ['F1', 'FORMULA1', 'FORMULA_1'] },
  { valor: 'F2', etiqueta: 'Formula 2', alias: ['F2', 'FORMULA2', 'FORMULA_2'] },
  { valor: 'F3', etiqueta: 'Formula 3', alias: ['F3', 'FORMULA3', 'FORMULA_3'] },
  { valor: 'F1_ACADEMY', etiqueta: 'F1 Academy', alias: ['F1_ACADEMY', 'F1 ACADEMY', 'F1ACADEMY'] },
];

export const TIPOS_SANCION: string[] = [
  'Amonestacion',
  'Penalizacion',
  'Suspension',
  'Descalificacion',
];

function normalizar(valor: string): string {
  return (valor ?? '').toString().toUpperCase().replace(/[^A-Z0-9]/g, '');
}

/** Indica si la categoria recibida del backend corresponde a la categoria conocida. */
export function coincideCategoria(valorCategoria: string, categoriaEvento: string): boolean {
  const categoria = CATEGORIAS.find((item) => item.valor === valorCategoria);
  if (!categoria) {
    return false;
  }
  const objetivo = normalizar(categoriaEvento);
  return categoria.alias.some((alias) => normalizar(alias) === objetivo);
}

/** Etiqueta legible para un valor de categoria cualquiera. */
export function etiquetaCategoria(valorCategoria: string): string {
  const objetivo = normalizar(valorCategoria);
  const encontrada = CATEGORIAS.find((item) => item.alias.some((alias) => normalizar(alias) === objetivo));
  return encontrada ? encontrada.etiqueta : valorCategoria;
}
