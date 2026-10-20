# FIA - Codigo Sprint 2 (Comision Violeta - implementadora)

Repositorio del Sprint 2 del proyecto **FIA** (Enunciado 1, APS 2026). Incluye el backend
REST, el frontend web, la base PostgreSQL y los datos semilla.

## Estructura

```
codigo/
├── docker-compose.yml        # PostgreSQL 16 (db fia / user fia / pass fia)
├── README.md                 # este archivo
├── backend/                  # Java 17 + Spring Boot 3.2 (Maven)
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── main/java/com/violeta/fia/
│       │   ├── FiaApplication.java
│       │   ├── config/        DataInitializer
│       │   ├── controller/    Auth, Sancion, Calendario, PruebaNeumatico, Usuario, GlobalExceptionHandler
│       │   ├── domain/        Usuario, Rol, Permiso, Sancion, SancionVisto,
│       │   │                  EventoCalendario, PruebaNeumatico, ResultadoPrueba, Categoria, RolNombre
│       │   ├── dto/           requests/responses (records)
│       │   ├── repository/    repositorios Spring Data JPA
│       │   ├── security/      JwtService, JwtAuthenticationFilter, SecurityConfig, CustomUserDetailsService
│       │   └── service/       AuthService, UsuarioService, SancionService,
│       │                      CalendarioService, PruebaNeumaticoService, PdfGenerator
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/  V1__schema.sql, V2__seed.sql
│       └── test/java/com/violeta/fia/
│           ├── BaseIntegrationTest.java
│           └── controller/    AuthControllerTest, AuthRegistroTest, SancionControllerTest,
│                              SancionVistoTest, SancionPermisosTest, CalendarioControllerTest,
│                              CalendarioFiltrosTest, PruebaNeumaticoControllerTest, InactividadTest
└── frontend/                 # Angular 17 standalone
    ├── package.json
    ├── angular.json
    ├── README.md
    └── src/
        ├── main.ts, index.html, styles.css
        ├── environments/     environment.ts, environment.prod.ts
        └── app/
            ├── app.component.ts, app.config.ts, app.routes.ts
            ├── core/         models, auth.service, guards, interceptor, api/calendario/sanciones/pruebas/usuarios services
            └── features/     login, dashboard, calendario, sanciones, pruebas-neumaticos
```

## Como levantar el entorno

```bash
# 1) Base de datos
docker compose up -d postgres

# 2) Backend (requiere JDK 17+; Maven o el wrapper incluido)
cd backend
mvn clean verify      # o ./mvnw clean verify
mvn spring-boot:run   # o ./mvnw spring-boot:run  -> http://localhost:8080

# 3) Frontend (requiere Node 18.19+/20.11+)
cd ../frontend
npm install
npm start             # http://localhost:4200
```

## Credenciales de prueba

| Usuario           | Contrasena     | Rol               |
| ----------------- | -------------- | ----------------- |
| `admin.fia`       | `Password123!` | `ADMIN_FIA`       |
| `admin.escuderia` | `Password123!` | `ADMIN_ESCUDERIA` |
| `publico`         | `Password123!` | `PUBLICO`         |


## Tests

```bash
cd backend && mvn test    # o ./mvnw test -> 70 tests
```

Cubren autenticacion (login, registro, 401/403), sanciones (ABM, validacion, permisos y "Visto"),
pilotos (ABM, limites y permisos), pruebas de neumaticos, filtros de calendario (categoria y año),
paridad entre la vista consolidada y el calendario y expiracion por inactividad. Usan H2 en memoria
(perfil `test`) con Flyway habilitado y `ddl-auto=validate`.
