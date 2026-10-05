# FIA - Frontend (Comision Violeta)

Aplicacion web en **Angular 17** (standalone components) para la gestion de la Federacion
Internacional de Automovilismo. Consume la API REST del backend en `http://localhost:8080/api`.

## Requisitos

- Node.js 18.19+ o 20.11+
- npm 10+
- Backend FIA corriendo en `http://localhost:8080`

## Como correr

```bash
npm install
npm start        # ng serve en http://localhost:4200
```

Otros scripts:

```bash
npm run build    # compila la aplicacion (produccion)
npm test         # pruebas unitarias con Karma
```

La URL de la API se configura en `src/environments/environment.ts` (desarrollo) y
`src/environments/environment.prod.ts` (produccion) mediante la propiedad `apiUrl`.

## Rutas

| Ruta                    | Acceso                             |
| ----------------------- | ---------------------------------- |
| `/login`                | Publica                            |
| `/dashboard`            | Usuarios autenticados              |
| `/calendario`           | Usuarios autenticados              |
| `/sanciones`            | `ADMIN_FIA`, `ADMIN_ESCUDERIA`     |
| `/pruebas-neumaticos`   | Usuarios autenticados              |
| `**`                    | Redirige a `/dashboard`            |

Toda ruta protegida exige sesion (`authGuard`). Sin sesion se redirige a `/login` conservando la
`returnUrl`. Las rutas por rol usan `roleGuard(...roles)`.

## Perfiles y credenciales de prueba

| Rol               | Usuario      | Contrasena   | Puede                                                                            |
| ----------------- | ------------ | ------------ | -------------------------------------------------------------------------------- |
| `ADMIN_FIA`       | `admin.fia`       | `Password123!` | Alta, edicion y baja de sanciones; calendario y pruebas.                      |
| `ADMIN_ESCUDERIA` | `admin.escuderia` | `Password123!` | Consulta de sanciones y boton "Visto" (registra fecha y usuario); calendario. |
| `PUBLICO`         | `publico`         | `Password123!` | Consulta de calendario y pruebas de neumaticos.                               |

> Las credenciales son de referencia para pruebas; dependen de los usuarios cargados en el backend.

## Funcionalidad

- **Autenticacion (US4):** login, guardas, interceptor que agrega `Authorization: Bearer` y ante
  `401` limpia la sesion y redirige a `/login`. La sesion se persiste en `localStorage` y expira
  segun `expiresIn`.
- **Sanciones (US3):** listado con filtros; alta/edicion/baja solo `ADMIN_FIA`; los selectores de
  piloto y escuderia se cargan desde `GET /api/usuarios`; una sancion requiere al menos piloto o
  escuderia; boton "Visto" solo `ADMIN_ESCUDERIA`, mostrando fecha y usuario.
- **Calendario (US2):** eventos agrupados en las cuatro categorias, orden cronologico, filtros por
  categoria, rango de fechas y circuito; descarga PDF/ICS con los mismos filtros; estado vacio.
- **Pruebas de neumaticos (US7):** selector de anio, dos fechas anuales, circuito, categorias y
  resultados por escuderia; solo lectura.

## Estructura

```
src/app/
  core/           servicios, modelos, guardas e interceptor
  features/       componentes por dominio (login, dashboard, calendario, sanciones, pruebas)
```
