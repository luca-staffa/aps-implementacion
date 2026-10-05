# Entrega Sprint 1 — Proyecto FIA (Enunciado 1)

**Comisión implementadora:** Violeta
**Rol en la dinámica de comisiones:** implementadora del proyecto FIA, bajo tutela de Malva (análisis y management).
**Integrantes:** Brugniere Sabrina · Staffa Luca · Tamburelli Alex Jeremias · Rosenzvaig Matias
**Stack:** Java 17 + Spring Boot 3.2 (Maven) · Angular 17 · PostgreSQL 16 · JWT · BCrypt
**Período del Sprint 1:** 14/09/2026 – 25/09/2026 (10 días hábiles)
**Entradas:** Anexo A = *Sprint 0 - Malva* (Product Backlog US1–US12) · Anexo B = *Enunciado APS 2026* · Anexo C = *Enunciado entrega Sprint 1*.

---

## 1. Sprint Goal

> **Consolidar la seguridad de la plataforma FIA** (autenticación con JWT, hash BCrypt, autorización por perfil con 401/403, expiración por inactividad e interfaz por rol) **y habilitar los primeros módulos de información oficial visibles para escuderías y público** (calendario de las cuatro categorías, sanciones y pruebas de neumáticos), sentando la base reutilizable sobre la que se apoyarán los módulos de los sprints siguientes.

El objetivo deja a la plataforma en un estado **usable y seguro de punta a punta**: un visitante puede registrarse e iniciar sesión, cada perfil ve exactamente lo que le corresponde y la información deportiva pública ya se consulta y se descarga. La seguridad (US4) es **habilitante**: sin ella no se puede restringir el acceso a sanciones ni construir el back-office de la FIA de los sprints 2 y 3.

---

## 2. Selección de User Stories (Sprint 1)

### 2.1 Candidatas y densidad de valor

Backlog de Malva excluyendo las US ya realizadas en el Sprint 0 (**US1**, **US8** y **US11**). Se ordena por valor de negocio por punto de historia (BV/SP).

| US | Título | SP | BV | BV/SP | Dependencias relevantes |
|----|--------|----|----|-------|--------------------------|
| US4 | Restricción de acceso por tipo de usuario | 8 | 8 | **1,00** | Habilitante de todo módulo restringido |
| US3 | Gestión de sanciones | 3 | 8 | **2,67** | Requiere US4 para el 403 |
| US2 | Consulta de calendarios | 2 | 5 | **2,50** | Independiente (lectura) |
| US7 | Consulta de pruebas de neumáticos | 2 | 4 | **2,00** | Independiente (lectura) |
| US9 | Asignación de roles a usuarios de escudería | 5 | 8 | **1,60** | Requiere US4 |
| US5 | Registro de controles técnicos | 8 | 8 | **1,00** | Requiere modelar controles y escuderías |
| US12 | Buscar información en el sistema | 5 | 5 | **1,00** | Solapa con filtros de US2/US7 |
| US10 | Restablecimiento de contraseña | 1 | 3 | **3,00** | Requiere servicio de correo (SMTP) |
| US6 | Sistema de mensajería interna | 5 | 3 | **0,60** | Requiere infraestructura de mensajería |

### 2.2 Regla de selección

1. **Dependencia habilitante primero:** US4 se incluye por regla, aunque su BV/SP (1,00) sea bajo, porque habilita la autorización por rol del resto de los módulos.
2. **Densidad BV/SP** para el remanente de capacidad (15 SP − 8 SP = 7 SP).
3. **Coherencia del Sprint Goal:** se priorizan módulos oficiales visibles (sanciones, calendario, pruebas) que cierran el objetivo “seguridad + módulos visibles”.
4. Un candidato de alta densidad como **US10** se difiere por una **dependencia técnica no disponible** (servicio de correo), criterio que prevalece sobre la densidad.

### 2.3 Seleccionadas

| US | Título | SP | BV | BV/SP | Responsable | Justificación |
|----|--------|----|----|-------|-------------|---------------|
| US4 | Restricción de acceso por tipo de usuario | 8 | 8 | 1,00 | Sabrina | Habilitante; base de seguridad y RBAC del sistema |
| US3 | Gestión de sanciones | 3 | 8 | 2,67 | Luca | Módulo oficial de mayor BV disponible; depende de US4 |
| US2 | Consulta de calendarios | 2 | 5 | 2,50 | Alex | Módulo público de valor alto y bajo costo |
| US7 | Consulta de pruebas de neumáticos | 2 | 4 | 2,00 | Matías | Cierra los módulos visibles con bajo riesgo |
| **Total** | | **15** | **25** | | | **= velocity del Sprint 0 (15 SP)** |

### 2.4 No incluidas y motivo

| US | SP | BV/SP | Motivo de exclusión |
|----|----|-------|---------------------|
| US10 | 1 | 3,00 | Mayor densidad del backlog, pero **requiere SMTP** aún no aprovisionado; se difiere a Sprint 2 para no introducir una dependencia externa en el sprint. |
| US9 | 5 | 1,60 | **Depende de US4** recién entregado; su secuencia natural es Sprint 2. |
| US5 | 8 | 1,00 | Compite en capacidad con la habilitante US4; queda para Sprint 2. |
| US12 | 5 | 1,00 | Solapa funcionalmente con los filtros de US2/US7 entregados en este sprint. |
| US6 | 5 | 0,60 | Baja densidad y requiere infraestructura de mensajería; se difiere (ver §8, donde pasa a ser prioritario por exigencia del enunciado). |

---

## 3. Sprint Backlog

### US4 — Restricción de acceso por tipo de usuario

- **Como** Administrador del sistema, **quiero** que el acceso a la información esté restringido según el tipo de usuario, **de forma que** se protejan los datos sensibles y se respeten los permisos correspondientes.
- **SP:** 8 · **BV:** 8 · **Responsable:** Sabrina · **Estimado:** 40 h

| # | Tarea | Horas |
|---|-------|-------|
| 1 | Diseñar el modelo `Usuario`/`Rol`/`Permiso` y la matriz RBAC de los 3 perfiles | 4 |
| 2 | Implementar entidades, repositorios y migración `V1__schema.sql` | 4 |
| 3 | Configurar Spring Security y `BCryptPasswordEncoder` | 6 |
| 4 | Implementar JWT (emisión/validación) y expiración por inactividad | 8 |
| 5 | Endpoints de registro/login/me/logout y control de 401 | 5 |
| 6 | Autorización por rol (403) y filtros de acceso a sanciones | 4 |
| 7 | Frontend: guards, interceptor, vistas por rol (3 perfiles) | 6 |
| 8 | Tests de integración: 401 sin token, 403 por rol, hash válido | 3 |
| | **Total** | **40** |

**Criterios de aceptación**
- Existen los 3 perfiles (ADMIN_FIA, ADMIN_ESCUDERIA, PUBLICO) con vistas y permisos diferenciados.
- Sin sesión, cualquier ruta privada redirige a login (frontend) y responde 401 (API).
- Un rol ajeno a un recurso recibe **403**.
- La contraseña se almacena con **hash** (BCrypt), nunca en texto plano.
- La sesión **expira por inactividad** (30 min configurable) además de por expiración absoluta.
- Queda documentada la **matriz RBAC**.

### US3 — Gestión de sanciones

- **Como** Administrador del sistema, **quiero** cargar y modificar las sanciones de cada categoría, **de forma que** se mantenga actualizada la información oficial.
- **SP:** 3 · **BV:** 8 · **Responsable:** Luca · **Estimado:** 15 h

| # | Tarea | Horas |
|---|-------|-------|
| 1 | Modelo `Sancion` + validación piloto **o** escudería + `V2__seed.sql` | 2 |
| 2 | ABM backend (alta/modificación/consulta/baja) con reglas | 4 |
| 3 | Endpoint "Visto" con fecha + usuario | 2 |
| 4 | Frontend: listado/alta/edición y control de acceso ADMIN_FIA | 5 |
| 5 | Tests de validación, 403 y "Visto" | 2 |
| | **Total** | **15** |

**Criterios de aceptación**
- El alta exige **piloto o escudería** + motivo + fecha + tipo, y es **visible al instante**.
- Si faltan ambos (piloto y escudería), el sistema **rechaza** el alta.
- La **modificación** impacta de inmediato en listados y consultas.
- El botón **"Visto"** registra fecha y usuario, visible en la sanción.
- Un usuario que no sea ADMIN_FIA recibe **403** en alta/modificación/baja.

### US2 — Consulta de calendarios

- **Como** Usuario, **quiero** consultar los calendarios de las distintas categorías, **de forma que** pueda conocer fechas y ubicaciones de las carreras.
- **SP:** 2 · **BV:** 5 · **Responsable:** Alex · **Estimado:** 12 h

| # | Tarea | Horas |
|---|-------|-------|
| 1 | Modelo `EventoCalendario` + categorías (F1, F2, F3, F1 Academy) | 2 |
| 2 | Endpoints con filtros (categoría, rango de fechas, circuito) | 2 |
| 3 | Vista Angular: 4 categorías agrupadas y orden cronológico | 3 |
| 4 | Filtros en pantalla | 2 |
| 5 | Exportación **PDF** e **ICS** idénticas a la pantalla | 2 |
| 6 | Estado vacío "No hay eventos…" + tests | 1 |
| | **Total** | **12** |

**Criterios de aceptación**
- Se visualizan las **4 categorías agrupadas** y **cronológicamente**.
- Los **filtros** (categoría, fechas, circuito) actualizan la vista.
- La **descarga PDF/ICS** refleja exactamente lo que está en pantalla (mismos filtros).
- Escuderías y público pueden consultar el calendario.
- Con filtros sin resultados se muestra **"No hay eventos para los filtros seleccionados"**.

### US7 — Consulta de pruebas de neumáticos

- **Como** Usuario, **quiero** consultar la información de las pruebas, **de forma que** pueda conocer los resultados de las distintas escuderías.
- **SP:** 2 · **BV:** 4 · **Responsable:** Matías · **Estimado:** 10 h

| # | Tarea | Horas |
|---|-------|-------|
| 1 | Modelo `PruebaNeumatico` (2 fechas anuales) + `ResultadoPrueba` + semilla | 2 |
| 2 | Endpoints de solo lectura + filtro por año | 2 |
| 3 | Vista Angular: fecha/circuito/categorías/resultados por escudería | 3 |
| 4 | Aviso de inicio de año | 2 |
| 5 | Tests de consulta | 1 |
| | **Total** | **10** |

**Criterios de aceptación**
- Se muestran las **dos fechas anuales** de pruebas.
- La vista incluye **fecha, circuito, categorías y resultados por escudería**.
- El módulo es de **solo lectura** (sin ABM).
- Existe un **aviso** de que las pruebas se anuncian al inicio del año.

**Total del Sprint Backlog: 77 h** (coincide con la capacidad del Sprint 0).

---

## 4. Detalle de desarrollo por US

> Resumen de ejecución. El código completo del Sprint 1 está en la carpeta `codigo/` de esta entrega.

### US4 — Seguridad y RBAC (Sabrina)

- **Hecho:** modelo de usuarios con roles y permisos, `SecurityConfig` stateless, `JwtService` (HS256, `jjwt`), `JwtAuthenticationFilter` con validación de expiración **absoluta** y por **inactividad** (`app.jwt.inactivity-timeout`), `CustomUserDetailsService`, `BCryptPasswordEncoder`, endpoints de autenticación y guards/interceptor en Angular.
- **Artefactos:** `security/`, `controller/AuthController.java`, `service/AuthService.java`, `domain/Usuario.java`, `domain/Rol.java`, `V1__schema.sql`; en el frontend `core/auth.*`, `core/role.guard.ts`, `core/auth.interceptor.ts`, `features/login`, `features/dashboard`.
- **Decisiones:** sesión *stateless* con JWT; el filtro actualiza `ultima_actividad` en cada request y devuelve **401** si supera el timeout; `entryPoint` 401 y `accessDeniedHandler` 403; roles `ADMIN_FIA`, `ADMIN_ESCUDERIA`, `PUBLICO`.
- **Resultado:** 3 perfiles operativos, matrix RBAC documentada, 401/403 verificados por tests.

### US3 — Sanciones (Luca)

- **Hecho:** ABM de sanciones, endpoint de "Visto", vista Angular con formulario condicionado por rol.
- **Artefactos:** `domain/Sancion.java`, `domain/SancionVisto.java`, `service/SancionService.java`, `controller/SancionController.java`; frontend `features/sanciones/*`, `core/sanciones.service.ts`.
- **Decisiones:** validación "piloto **o** escudería" en el servicio (400 si faltan ambos), además de un `CHECK` en la BD como defensa; el `categoria` se agregó como campo filtrable; solo `ADMIN_FIA` modifica y `ADMIN_ESCUDERIA` marca "Visto".
- **Resultado:** alta con visibilidad inmediata, rechazo correcto y "Visto" con fecha + usuario.

### US2 — Calendario (Alex)

- **Hecho:** modelo de eventos por categoría, endpoints con filtros, vista agrupada y exportadores PDF/ICS.
- **Artefactos:** `domain/EventoCalendario.java`, `service/CalendarioService.java`, `service/PdfGenerator.java`, `controller/CalendarioController.java`; frontend `features/calendario/*`, `core/calendario.service.ts`.
- **Decisiones:** agrupación por las 4 categorías con orden cronológico; el PDF se genera sin dependencias externas (writer mínimo) y el ICS manualmente (fechas `VALUE=DATE`), de modo que la descarga usa los mismos filtros de la pantalla.
- **Resultado:** 4 categorías agrupadas, filtros funcionales, descargas equivalentes y estado vacío.

### US7 — Pruebas de neumáticos (Matías)

- **Hecho:** modelo de pruebas y resultados, endpoints de solo lectura, vista con selector de año.
- **Artefactos:** `domain/PruebaNeumatico.java`, `domain/ResultadoPrueba.java`, `service/PruebaNeumaticoService.java`, `controller/PruebaNeumaticoController.java`; frontend `features/pruebas-neumaticos/*`, `core/pruebas.service.ts`.
- **Decisiones:** resultados guardados por escudería (texto) porque los pilotos de las pruebas no son usuarios del sistema; solo lectura y aviso en la vista.
- **Resultado:** dos fechas anuales con información completa y sin ABM.

### Integración y cierre

- **Integración:** `docker-compose.yml` con PostgreSQL 16; Flyway aplica `V1__schema.sql` y `V2__seed.sql`; `DataInitializer` garantiza roles y credenciales; CORS habilitado para `http://localhost:4200`.
- **Cierre:** regresión de backend (`./mvnw test`, 31 tests) + verificación de flujos por rol con el frontend; demo con Malva sobre los cuatro módulos; alta de esta documentación y entrega del código.

---

## 5. Minutas diarias (Daily meetings)

**Reunión:** 10:00 h, 15 min, todos los integrantes. Formato: Hecho / Hará / Impedimentos.

### Día 1 — Lunes 14/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | Modelo Usuario/Rol/Permiso | Configurar Spring Security | Ninguno |
| Luca | Análisis de campos de Sanción | Modelo y migración de Sanción | Ninguno |
| Alex | Análisis modelo event/calendario | Entidad EventoCalendario | Ninguno |
| Matías | Análisis pruebas de neumáticos | Entidades Prueba/Resultado | Ninguno |

### Día 2 — Martes 15/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | Security config + BCrypt | JWT e inactividad | Ninguno |
| Luca | Modelo Sanción + validación | ABM backend | Ninguno |
| Alex | EventoCalendario + repo | Endpoints con filtros | Ninguno |
| Matías | Prueba/Resultado + repo | Endpoints solo lectura | Ninguno |

### Día 3 — Miércoles 16/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | JWT + filtro de inactividad | Endpoints de auth y 401 | Ninguno |
| Luca | ABM backend de sanciones | Endpoint "Visto" | Ninguno |
| Alex | Endpoints de calendario | Vista Angular de calendario | Ninguno |
| Matías | Endpoints de pruebas | Vista Angular de pruebas | Ninguno |
| **Equipo** | **US7 completada (demo interna)** | | |

### Día 4 — Jueves 17/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | Endpoints auth + 401 | Autorización 403 y matriz RBAC | Ninguno |
| Luca | "Visto" + fecha/usuario | Frontend de sanciones | Ninguno |
| Alex | Vista calendario 4 categorías | Filtros de calendario | Ninguno |
| Matías | Vista pruebas | Aviso y detalles de UI | Ninguno |

### Día 5 — Viernes 18/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | Autorización por rol (403) | Frontend: guards e interceptor | Ninguno |
| Luca | Frontend sanciones | Tests de sanciones | Ninguno |
| Alex | Filtros de calendario | Export PDF/ICS | Ninguno |
| Matías | Aviso de pruebas + UI | Tests de pruebas | Ninguno |
| **Equipo** | **US2 completada (demo interna)** | | |

### Día 6 — Lunes 21/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | Guards + interceptor | Vistas por rol (3 perfiles) | Guards Angular a reescribir |
| Luca | Tests de sanciones | Ajustes de validación | Ninguno |
| Alex | Export ICS | Verificación PDF/ICS vs pantalla | Zona horaria del ICS |
| Matías | Tests de pruebas | Semilla de resultados | Ninguno |

### Día 7 — Martes 22/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | Vistas por rol | Tests 401/403 backend | Ninguno |
| Luca | Ajustes de validación | Cierre US3 | Ninguno |
| Alex | Export ICS + PDF | Estado vacío y polish | Zona horaria resuelta |
| Matías | Semilla de pruebas | Cierre US7 | Ninguno |
| **Equipo** | **US3 completada (demo interna)** | | |

### Día 8 — Miércoles 23/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | Tests 401/403 | Cierre US4 + matriz RBAC | Inactividad: casos borde |
| Luca | US3 cerrada | Apoyo en integración | Ninguno |
| Alex | US2 cerrada | Apoyo en integración | Ninguno |
| Matías | US7 cerrada | Apoyo en integración | Ninguno |

### Día 9 — Jueves 24/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | US4 casi cerrada | Cierre US4 | Casos borde de inactividad |
| Luca | Integración de sanciones | Regresión integrada | Ninguno |
| Alex | Integración de calendario | Regresión integrada | Ninguno |
| Matías | Integración de pruebas | Regresión integrada | Ninguno |

### Día 10 — Viernes 25/09/2026
| Integrante | Hecho | Hará | Impedimentos |
|---|---|---|---|
| Sabrina | US4 cerrada + matriz RBAC | Demo y documentación | Ninguno |
| Luca | Regresión OK | Demo de US3 | Ninguno |
| Alex | Regresión OK | Demo de US2 | Ninguno |
| Matías | Regresión OK | Demo de US7 | Ninguno |
| **Equipo** | **Sprint 1 cerrado: 15/15 SP** | **Preparar Sprint 2** | Ninguno |

---

## 6. Retrospectiva del Sprint 1

### 6.1 Cómo monitoreamos el avance

- **Daily meetings** diarios de 15 minutos (10:00 h) con el formato Hecho / Hará / Impedimentos.
- **Tablero Scrum** con columnas *To Do / In Progress / Done* y una tarjeta por tarea del Sprint Backlog.
- **Burndown** actualizado al cierre de cada daily, tomando los SP restantes del tablero (tabla en §7).
- **Demo interna** al completar cada US (días 3, 5 y 7) para validar criterios con el equipo antes del cierre.

### 6.2 Problemas surgidos

| # | Tipo | Problema | Impacto | Acción tomada |
|---|------|----------|---------|---------------|
| 1 | Técnico | La expiración por inactividad tenía casos borde (usuario con token válido pero inactivo) | +4 h en US4 | Se movió la verificación al `JwtAuthenticationFilter` con `ultima_actividad` |
| 2 | Técnico | El export ICS no reflejaba bien las fechas por zona horaria | +3 h en US2 | Se fijaron fechas `VALUE=DATE` y se igualaron los filtros a pantalla |
| 3 | Proceso | Los guards de Angular se escribieron dos veces (primero por ruta, luego por rol) | +2 h en US4 | Se unificó en `authGuard` + `roleGuard(...)` |
| 4 | Comunicación | El contrato de `Sancion` agregó `categoria` para poder filtrar, no previsto en el backlog | Sin desvío | Se documentó como supuesto |

### 6.3 Análisis de estimaciones

| US | Horas estimadas | Horas reales | Desvío (h) | Desvío (%) | Causa principal |
|----|-----------------|--------------|------------|------------|-----------------|
| US4 | 40 | 46 | +6 | +15,0 % | Complejidad del control de inactividad y reescritura de guards |
| US3 | 15 | 14 | −1 | −6,7 % | Validación y RBAC reutilizados de US4 |
| US2 | 12 | 15 | +3 | +25,0 % | Formato ICS y manejo de zonas horarias |
| US7 | 10 | 9 | −1 | −10,0 % | Módulo de solo lectura, simple |
| **Total** | **77** | **84** | **+7** | **+9,1 %** | La estimación fue **conservadora**: el desvío se explica por la curva de aprendizaje de JWT + Angular |

**Conclusión:** la estimación fue razonable (±10 % global), con la mayor imprecisión en las US de mayor incertidumbre técnica. Para el Sprint 2 se recomienda mantener el colchón y no comprometer más de **14 SP**.

### 6.4 Qué funcionó / qué no / acciones de mejora

| Qué funcionó | Qué no funcionó |
|---|---|
| La seguridad como habilitante: US3 no tuvo retrabajo de autorización | El encare inicial de US2 (ICS) fue optimista |
| Las demos internas por US detectaron problemas temprano | La estimación no separó “aprendizaje de framework” de “lógica de negocio” |
| La reutilización de servicios/validaciones entre US | El tablero se actualizó tarde algunos días |

| Acción de mejora | Responsable | Plazo |
|---|---|---|
| Estimar explícitamente una reserva por curva de aprendizaje en US con tecnologías nuevas | Staffa Luca | Inicio Sprint 2 |
| Definir el contrato de API antes de empezar el frontend de cada US | Brugniere Sabrina | Inicio Sprint 2 |
| Actualizar el burndown antes de cerrar la daily, sin excepción | Tamburelli Alex | Permanente |
| Agregar un test de exportación ICS/PDF antes de dar por cerrada una US de calendario | Rosenzvaig Matias | Sprint 2 |

---

## 7. Métricas del Sprint 1

| Métrica | Valor |
|---|---|
| SP comprometidos | 15 |
| SP completados | 15 |
| **Velocity (SP)** | **15** |
| Horas estimadas | 77 |
| Horas reales | 84 |
| Desvío de esfuerzo | +9,1 % |
| Cumplimiento del Sprint Goal | 100 % (US2, US3, US4 y US7 en Done) |

### Burndown (SP restantes al cierre de cada día)

| Día | Fecha | SP restantes (real) | SP restantes (ideal) |
|-----|-------|---------------------|----------------------|
| 0 | inicio 14/09 | 15 | 15,0 |
| 1 | 14/09 | 15 | 13,5 |
| 2 | 15/09 | 15 | 12,0 |
| 3 | 16/09 | 13 | 10,5 |
| 4 | 17/09 | 13 | 9,0 |
| 5 | 18/09 | 11 | 7,5 |
| 6 | 21/09 | 11 | 6,0 |
| 7 | 22/09 | 8 | 4,5 |
| 8 | 23/09 | 8 | 3,0 |
| 9 | 24/09 | 4 | 1,5 |
| 10 | 25/09 | 0 | 0,0 |

*Lectura: el equipo quemó menos SP en la primera mitad (seguridad) y recuperó en la segunda, cerrando en 0 al final del día 10.*

---

## 8. Planificación del Sprint 2

### 8.1 Sprint Goal

> **Extender la plataforma con los módulos operativos de la FIA y la comunicación entre usuarios**, incorporando el registro de controles técnicos, la mensajería/notificaciones internas (prioridad explícita del enunciado) y el restablecimiento de contraseña, reutilizando la seguridad y el RBAC ya consolidados en el Sprint 1.

### 8.2 Estimación

**Tabla BV/SP del remanente (tras Sprint 1):**

| US | Título | SP | BV | BV/SP | Dependencias |
|----|--------|----|----|-------|--------------|
| US10 | Restablecimiento de contraseña | 1 | 3 | **3,00** | Requiere SMTP (disponible en Sprint 2) |
| US9 | Asignación de roles a usuarios de escudería | 5 | 8 | **1,60** | Requiere US4 (entregada) |
| US5 | Registro de controles técnicos | 8 | 8 | **1,00** | Requiere modelar escuderías |
| US12 | Buscar información en el sistema | 5 | 5 | **1,00** | Requiere histórico de US2/US7 |
| US6 | Sistema de mensajería interna | 5 | 3 | **0,60** | Requiere usuarios y RBAC (US4) |

**Capacidad ajustada:** velocity real 15 SP, corregida a la baja por el desvío de esfuerzo (+9,1 %) → **capacity ≈ 14 SP**.

**Selección final (14 SP):**

| US | Título | SP | BV | BV/SP | Horas est. | Justificación |
|----|--------|----|----|-------|-----------|---------------|
| US5 | Registro de controles técnicos | 8 | 8 | 1,00 | 42 | Módulo oficial de alto BV y base del back-office FIA |
| US6 | Sistema de mensajería interna | 5 | 3 | 0,60 | 26 | **Prioridad explícita del enunciado**; se apoya en la seguridad de US4 |
| US10 | Restablecimiento de contraseña | 1 | 3 | 3,00 | 8 | Máxima densidad; el servicio de correo ya puede aprovisionarse |
| **Total** | | **14** | **14** | | **76** | |

**No incluidas en Sprint 2:**

| US | Motivo |
|----|--------|
| US9 | BV alto pero secuencia posterior a la mensajería; candidata N.º 1 del Sprint 3 |
| US12 | Requiere que exista histórico suficiente de calendario/pruebas; Sprint 3 |


### 8.3 Sprint Backlog

**US5 — Registro de controles técnicos (8 SP · 42 h · Alex)**
- Modelo de control técnico (fecha, tipo, escudería, resultado pasa/no pasa) — 4 h.
- ABM backend (solo ADMIN_FIA) + consulta para escuderías y público — 12 h.
- Vista Angular de carga y de consulta — 14 h.
- Validaciones y visibilidad inmediata — 6 h.
- Tests de ABM, permisos y reglas — 6 h.
- **Criterios:** ADMIN_FIA registra fecha y tipo de cada control indicando pasa/no pasa; escuderías y público lo visualizan; 403 a roles no autorizados.

**US6 — Sistema de mensajería interna (5 SP · 26 h · Matías)**
- Modelo de mensaje/notificación (emisor, destinatario, asunto, cuerpo, leído) — 4 h.
- Endpoints de envío y bandeja — 8 h.
- Vista de mensajería y contador de no leídos — 9 h.
- Endurecimiento de seguridad (validación de destinatario y autorización) — 3 h.
- Tests — 2 h.
- **Criterios:** un usuario envía mensajes/notificaciones a otros usuarios y escuderías/FIA; solo el destinatario accede a su bandeja (403 al resto); los mensajes se marcan como leídos.

**US10 — Restablecimiento de contraseña (1 SP · 8 h · Luca)**
- Endpoint de solicitud de restablecimiento (token temporal) — 3 h.
- Integración con el servicio de correo (SMTP) — 3 h.
- Vista de solicitud y de cambio de contraseña — 1 h.
- Tests (token válido/expirado) — 1 h.
- **Criterios:** el usuario solicita el restablecimiento y recupera el acceso con un token válido y de vida limitada; el token expirado/inválido es rechazado.

### 8.4 Monitoreo y riesgos del Sprint 2

**Monitoreo:** daily de 15 min (Hecho/Hará/Impedimentos), tablero Scrum, burndown diario y demo interna por US. Revisión de mitad de sprint para reasignar capacidad si US5 se desvía.

| Riesgo | Prob. | Impacto | Mitigación |
|--------|-------|---------|------------|
| El SMTP retrasa US10 | Media | Medio | Proveedor de correo de prueba (p. ej. Mailtrap) desde el día 1 |
| US5 subestima el modelado de escuderías | Media | Alto | Spike de 4 h el día 1 para cerrar el modelo antes de implementar |
| US6 (mensajería) crece por notificaciones en tiempo real | Media | Medio | Alcance acotado a mensajería persistente y notificación en bandeja; sin websockets |
| Sobrecarga de Alex (US5 grande) | Media | Medio | Apoyo de Matías una vez cerrada US6 |
| Regresión en seguridad por nuevos endpoints | Baja | Alto | Reutilizar `SecurityConfig` y sumar tests 401/403 en cada US |

---

## Supuestos

1. **US4 es habilitante:** se prioriza por dependencia, no por densidad (BV/SP = 1,00).
2. **US10 se excluye del Sprint 1** por requerir SMTP, no por su BV/SP (el más alto del backlog).
3. **US6 se adelanta en el Sprint 2** por exigencia explícita del enunciado, aun con BV 3 asignado por Malva; se reemplaza a US9 (BV 8) en la selección.
4. **Capacidad del Sprint 2 = 14 SP**, ajustada a la baja desde la velocity de 15 SP por el desvío de esfuerzo de +9,1 % del Sprint 1.
5. **Horas reales del Sprint 1** corresponden a la ejecución de esta comisión implementadora; el desglose estimado/real es consistente con la velocity base (15 SP / 77 h).
6. **`Sancion.categoria`** es un campo agregado para permitir el filtrado por categoría exigido por la vista de sanciones; no figuraba como campo en el Sprint 0 de Malva.
7. **Los pilotos de las pruebas de neumáticos** se modelan como texto (no como usuarios del sistema), porque el enunciado no los trata como cuentas de usuario.
8. **Fecha de entrega del Sprint 1** asumida como 25/09/2026, según el período de 10 días hábiles indicado en la planificación.
9. **La app móvil** mencionada en el enunciado no se aborda en el Sprint 1 ni en la planificación del Sprint 2; se reserva para sprints posteriores.
