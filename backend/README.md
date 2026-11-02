# Backend FIA - Comision Violeta

Backend REST del proyecto FIA (APS 2026) construido con Java 17 y Spring Boot 3.2.

## Stack

- Java 17
- Spring Boot 3.2.5 (Web, Security, Data JPA, Validation)
- Maven
- PostgreSQL + Flyway
- JWT (jjwt 0.12.x) + BCrypt

## Requisitos

- JDK 17 o superior
- Maven 3.9 o superior
- PostgreSQL 14 o superior

## Base de datos

Crear la base y el usuario:

```sql
CREATE USER fia WITH PASSWORD 'fia';
CREATE DATABASE fia OWNER fia;
```

Las migraciones de `src/main/resources/db/migration` se ejecutan automaticamente con Flyway:

- `V1__schema.sql`: creacion de tablas e indices.
- `V2__seed.sql`: datos iniciales (roles, usuarios, calendario, sanciones y pruebas de neumaticos).
- `V3__categoria_temporada_atributos.sql`: catalogo de categorias, temporadas y atributos.
- `V4__pilotos_escuderias.sql`: escuderias y entidad piloto.
- `V5__perfil_y_sanciones.sql`: telefono de usuario y campos de sancion.
- `V6__calendario_demo.sql`, `V7__sanciones_piloto_escuderia.sql`, `V8__temporadas_demo.sql`: datos demo y ajustes.
- `V9__controles_tecnicos.sql`: tabla `control_tecnico` y controles de demo.
- `V10__roles_escuderia.sql`: columna `usuario.rol_escuderia` con sus roles internos.
- `V11__prueba_compuesto.sql`: compuesto de neumático en cada resultado de prueba.

Al arrancar, `DataInitializer` garantiza los roles, las credenciales iniciales y los miembros demo de cada escuderia.

## Configuracion

Variables en `src/main/resources/application.yml`:

| Propiedad | Valor por defecto |
| --- | --- |
| `server.port` | `8080` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/fia` |
| `spring.datasource.username` | `fia` |
| `spring.datasource.password` | `fia` |
| `app.jwt.secret` | clave configurable |
| `app.jwt.expiracion` | `3600000` ms |
| `app.jwt.inactivity-timeout` | `1800000` ms |
| `app.cors.allowed-origins` | `http://localhost:4200` |

Se recomienda sobrescribir `app.jwt.secret`, el datasource y las credenciales con variables de entorno en produccion.

## Como ejecutar

```bash
# Compilar y testear (Maven del sistema)
mvn clean verify

# Levantar la aplicacion
mvn spring-boot:run
```

Si no tenes Maven instalado, el proyecto incluye el Maven Wrapper:

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

## Credenciales de prueba

| Usuario | Contrasena | Rol global | Escuderia / rol interno |
| --- | --- | --- | --- |
| `admin.fia` | `Password123!` | `ADMIN_FIA` | - |
| `admin.escuderia` | `Password123!` | `ADMIN_ESCUDERIA` | Alpine / `ADMINISTRADOR` |
| `admin.ferrari` | `Password123!` | `ADMIN_ESCUDERIA` | Ferrari / `ADMINISTRADOR` |
| `editor.alpine` | `Password123!` | `PUBLICO` | Alpine / `EDITOR` |
| `lector.alpine` | `Password123!` | `PUBLICO` | Alpine / `LECTOR` |
| `editor.ferrari` | `Password123!` | `PUBLICO` | Ferrari / `EDITOR` |
| `publico` | `Password123!` | `PUBLICO` | - |

## Endpoints

### Autenticacion

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/auth/register` | Publico |
| POST | `/api/auth/login` | Publico |
| GET | `/api/auth/me` | Autenticado |
| PUT | `/api/auth/me` | Autenticado (solo los datos propios) |
| POST | `/api/auth/logout` | Autenticado |

`PUT /api/auth/me` permite actualizar `email` y `telefono` del usuario autenticado. El email debe tener formato valido y ser unico; el telefono se valida con el patron `^\+?[0-9 ()-]{7,20}$`.

### Usuarios

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/usuarios` | `ADMIN_FIA` |
| GET | `/api/usuarios/{id}` | `ADMIN_FIA` |

### Escuderias y roles internos

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/escuderias` | Publico |
| PUT | `/api/escuderias/{id}` | `ADMIN_FIA` o miembro con rol `ADMINISTRADOR`/`EDITOR` de esa escuderia |
| GET | `/api/escuderias/miembros` | `ADMIN_ESCUDERIA` (solo su escuderia) |
| PUT | `/api/escuderias/miembros/{usuarioId}` | `ADMIN_ESCUDERIA` (solo su escuderia) |
| DELETE | `/api/escuderias/miembros/{usuarioId}/rol` | `ADMIN_ESCUDERIA` (solo su escuderia) |

El rol global (`ADMIN_FIA`, `ADMIN_ESCUDERIA`, `PUBLICO`) define el acceso a la plataforma; el rol de escuderia (`ADMINISTRADOR`, `EDITOR`, `LECTOR`) define que puede hacer un miembro dentro de su escuderia. El administrativo de escuderia solo puede ver y modificar los roles de los miembros de su propia escuderia. Un miembro con rol `ADMINISTRADOR` o `EDITOR` puede actualizar la informacion de su escuderia; `LECTOR` solo consulta.

### Controles tecnicos

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/controles-tecnicos?escuderiaId=` | Publico |
| GET | `/api/controles-tecnicos/{id}` | Publico |
| POST | `/api/controles-tecnicos` | `ADMIN_FIA` |
| PUT | `/api/controles-tecnicos/{id}` | `ADMIN_FIA` |

Un control tecnico registra el `escuderiaId`, el `auto`, la `fecha`, los `elementosVerificados`, el `resultado` (`APTO` / `NO_APTO`) y `observaciones` opcionales. El alta y la modificacion quedan restringidas al administrativo de la FIA; las escuderias y el publico solo consultan.

### Sanciones

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/sanciones?categoria=&desde=&hasta=&pilotoId=&escuderiaId=&eventoId=` | `ADMIN_FIA` |
| GET | `/api/sanciones/{id}` | `ADMIN_FIA` |
| POST | `/api/sanciones` | `ADMIN_FIA` |
| PUT | `/api/sanciones/{id}` | `ADMIN_FIA` |
| DELETE | `/api/sanciones/{id}` | `ADMIN_FIA` |
| POST | `/api/sanciones/{id}/visto` | `ADMIN_FIA` |

El modulo de sanciones esta restringido al Administrador del sistema (`ADMIN_FIA`). Las sanciones requieren `categoria`, `eventoId` (carrera), `motivo`, `fecha` y `tipo` (penalidad), y se asocian a un piloto (`pilotoId`, entidad `piloto`) o a una escuderia (`escuderiaId`, entidad `escuderia`). El `DELETE` es una baja logica: la sancion deja de listarse pero permanece en la base de datos. Los filtros `pilotoId`, `escuderiaId` y `eventoId` permiten consultar el historial del piloto, la escuderia o la carrera.

### Calendario

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/calendario/eventos?categoria=&anio=&desde=&hasta=&circuito=` | Publico |
| GET | `/api/calendario/eventos/{id}` | Publico |
| GET | `/api/calendario/eventos/export.pdf` | Publico |
| GET | `/api/calendario/eventos/export.ics` | Publico |

### Pruebas de neumaticos

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/pruebas-neumaticos?anio=&escuderia=` | Publico |
| GET | `/api/pruebas-neumaticos/{id}` | Publico |

El filtro opcional `escuderia` devuelve unicamente los resultados de las escuderias cuyo nombre contenga el valor indicado.

## Autenticacion JWT

Las rutas protegidas requieren el encabezado:

```
Authorization: Bearer <token>
```

El token se expira por tiempo absoluto (`app.jwt.expiracion`) y por inactividad (`app.jwt.inactivity-timeout`).

## Tests

```bash
# Suite completa: 93 tests
mvn test
# o bien
./mvnw test
```

Incluye tests de autenticacion (login/registro/401/403), sanciones (ABM, validacion, permisos),
pilotos (ABM y limites), pruebas de neumaticos (incluido el filtro por escuderia), filtros de
calendario, registro de "Visto", controles tecnicos (ABM, validacion y permisos), asignacion de
roles de escuderia y expiracion por inactividad.

Los tests usan H2 en memoria con el perfil `test` (`src/test/resources/application-test.yml`), Flyway habilitado y `ddl-auto=validate`.
