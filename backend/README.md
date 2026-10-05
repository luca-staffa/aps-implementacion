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

Al arrancar, `DataInitializer` garantiza los roles y las credenciales iniciales.

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

| Usuario | Contrasena | Rol |
| --- | --- | --- |
| `admin.fia` | `Password123!` | `ADMIN_FIA` |
| `admin.escuderia` | `Password123!` | `ADMIN_ESCUDERIA` |
| `publico` | `Password123!` | `PUBLICO` |

## Endpoints

### Autenticacion

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/auth/register` | Publico |
| POST | `/api/auth/login` | Publico |
| GET | `/api/auth/me` | Autenticado |
| POST | `/api/auth/logout` | Autenticado |

### Usuarios

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/usuarios` | `ADMIN_FIA` |
| GET | `/api/usuarios/{id}` | `ADMIN_FIA` |

Se usa para poblar los selectores de piloto y escuderia al registrar sanciones.

### Sanciones

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/sanciones?categoria=&desde=&hasta=` | `ADMIN_FIA`, `ADMIN_ESCUDERIA`, `PUBLICO` |
| GET | `/api/sanciones/{id}` | `ADMIN_FIA`, `ADMIN_ESCUDERIA`, `PUBLICO` |
| POST | `/api/sanciones` | `ADMIN_FIA` |
| PUT | `/api/sanciones/{id}` | `ADMIN_FIA` |
| DELETE | `/api/sanciones/{id}` | `ADMIN_FIA` |
| POST | `/api/sanciones/{id}/visto` | Autenticado |

### Calendario

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/calendario/eventos?categoria=&desde=&hasta=&circuito=` | Publico |
| GET | `/api/calendario/eventos/{id}` | Publico |
| GET | `/api/calendario/eventos/export.pdf` | Publico |
| GET | `/api/calendario/eventos/export.ics` | Publico |

### Pruebas de neumaticos

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/api/pruebas-neumaticos?anio=` | Publico |
| GET | `/api/pruebas-neumaticos/{id}` | Publico |

## Autenticacion JWT

Las rutas protegidas requieren el encabezado:

```
Authorization: Bearer <token>
```

El token se expira por tiempo absoluto (`app.jwt.expiracion`) y por inactividad (`app.jwt.inactivity-timeout`).

## Tests

```bash
# Suite completa: 31 tests
mvn test
# o bien
./mvnw test
```

Incluye tests de autenticacion (login/registro/401/403), sanciones (ABM, validacion, permisos),
pruebas de neumaticos, filtros de calendario, registro de "Visto" y expiracion por inactividad.

Los tests usan H2 en memoria con el perfil `test` (`src/test/resources/application-test.yml`), Flyway deshabilitado y `ddl-auto=create-drop`.
