# Historia 3 — Controles técnicos, roles de escudería y consulta de pruebas

## Alcance

Este sprint incorpora tres funcionalidades del backlog:

1. **Registro de controles técnicos**: el administrativo de la FIA carga y modifica los
   controles técnicos de los autos de todas las escuderías; las escuderías y el público consultan.
2. **Asignación de roles a usuarios de escudería**: el administrativo de escudería asigna,
   modifica y revoca roles internos entre los miembros de su escudería.
3. **Consulta de pruebas de neumáticos**: consulta pública de pruebas y resultados por
   escudería. La funcionalidad ya existía y en este sprint se completó con el filtro por escudería y
   con el compuesto de neumático en cada resultado.

## Modelo y relaciones

### Control técnico

-`control_tecnico` referencia una `escuderia` (obligatoria) y opcionalmente un `piloto`.
- Campos: `auto` (identificación del auto), `fecha`, `elementos_verificados`, `resultado`
  (`APTO` / `NO_APTO`), `observaciones` y `fecha_creacion`.
- Si se elimina un piloto, la referencia del control queda en `NULL` (`ON DELETE SET NULL`) para
  no perder el registro histórico del control.

### Roles de escudería

- `usuario.rol_escuderia` almacena el rol interno del miembro: `ADMINISTRADOR`, `EDITOR` o `LECTOR`.
- El rol global (`rol_id`) sigue definiendo el acceso a la plataforma; el rol interno define qué
  puede hacer el miembro dentro de su escudería.
- `ADMINISTRADOR` y `EDITOR` pueden actualizar la información de la escudería; `LECTOR` solo consulta.
- El administrativo de escudería (`ADMIN_ESCUDERIA`) es quien administra los roles internos.

## Reglas de negocio

1. Solo `ADMIN_FIA` puede crear o modificar controles técnicos; el resto de los roles recibe 403.
2. La consulta de controles técnicos es pública y filtrable por escudería.
3. Un control requiere escudería, auto, fecha, elementos verificados y resultado; la fecha no puede
   ser futura. Los datos incompletos o inválidos se rechazan con 400 y un mensaje claro.
4. Solo `ADMIN_ESCUDERIA` puede listar y modificar los roles internos, y únicamente dentro de su
   propia escudería.
5. Un administrativo no puede ver ni modificar roles de usuarios de otra escudería (403).
6. Un miembro con rol `ADMINISTRADOR` o `EDITOR` puede actualizar la información de su escudería;
   un `LECTOR` recibe 403.
7. La consulta de pruebas de neumáticos es de solo lectura y admite filtro por escudería.

## Endpoints principales

- `GET /api/controles-tecnicos?escuderiaId=` — consulta pública.
- `GET /api/controles-tecnicos/{id}` — detalle público.
- `POST /api/controles-tecnicos` — alta (`ADMIN_FIA`).
- `PUT /api/controles-tecnicos/{id}` — modificación (`ADMIN_FIA`).
- `GET /api/escuderias/miembros` — miembros de la escudería del administrativo.
- `PUT /api/escuderias/miembros/{usuarioId}` — asignar o modificar rol interno.
- `DELETE /api/escuderias/miembros/{usuarioId}/rol` — revocar rol interno.
- `PUT /api/escuderias/{id}` — actualizar información de la escudería según el rol interno.
- `GET /api/pruebas-neumaticos?anio=&escuderia=` — consulta pública filtrable.

## Permisos esperados

- `POST`/`PUT` de controles: 201/200 con `ADMIN_FIA`; 403 con otros roles; 401 sin token.
- Listado de controles y pruebas: 200 sin token.
- Gestión de miembros: 200 con `ADMIN_ESCUDERIA`; 403 con `ADMIN_FIA` o `PUBLICO`.
- Asignar rol a un usuario de otra escudería: 403.

## Observaciones de diseño

- Se reutilizan las entidades `Escuderia` y `Piloto` creadas en historias previas.
- El rol interno se modela como enum persistido (`VARCHAR`) con una restricción `CHECK` en la base.
- El catálogo de escuderías (`GET /api/escuderias`) es público para que las vistas de controles y
  pruebas puedan filtrar por escudería; la gestión de miembros queda en una ruta protegida aparte.
- Las pruebas de neumáticos ya existían; el filtro por escudería se resolvió en la capa de servicio
  sobre los resultados de cada prueba y el `compuesto` se agregó como columna de `resultado_prueba`.

## Limitaciones y deuda técnica

- El "auto" se identifica con un texto libre (por ejemplo, el código o número del monoplaza); no se
  modeló una entidad `Auto` independiente porque no era necesaria para el alcance actual.
- La vista de gestión de personal está disponible para el administrativo de escudería; la
  actualización de información por parte de un miembro se valida en el backend y puede exponerse en
  una pantalla propia en un sprint posterior.
- La interfaz móvil mencionada en el enunciado continúa fuera del alcance de este sprint.
