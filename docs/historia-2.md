# Historia 2 — Registro y gestión de pilotos

## Alcance

La historia 2 implementa la gestión de pilotos asociada a una escudería, con reglas de negocio, permisos por rol y exposición pública limitada.

## Modelo y relaciones

- Cada piloto pertenece a exactamente una categoria y a una temporada.
- Cada piloto pertenece a una escudería.
- La escudería del administrador de escudería se resuelve desde `usuario.escuderia_id`.
- El rol del piloto se modela con `TITULAR` o `SUPLENTE`.
- El número y la abreviatura son únicos por `(categoria_id, temporada_id)`.

## Reglas de negocio

1. `ADMIN_FIA` tiene solo lectura sobre pilotos; no puede crear, editar ni borrar.
2. `ADMIN_ESCUDERIA` solo puede operar sobre pilotos de su propia escudería.
3. El límite de titular/suplente se valida por categoria usando atributos `max_titulares` y `max_suplentes`.
4. El endpoint público no devuelve `fechaNacimiento` ni `numeroLicenciaFia`.
5. La API acepta `escuderiaId` solo para FIA; la escudería del administrador se fija automáticamente.

## Endpoints principales

- `GET /api/pilotos` — listado protegido por rol.
- `GET /api/pilotos/{id}` — detalle protegido.
- `POST /api/pilotos` — alta de piloto.
- `PUT /api/pilotos/{id}` — edición.
- `DELETE /api/pilotos/{id}` — baja lógica.
- `GET /api/pilotos/publico` — consulta pública filtrable.

## Permisos esperados

- 200 con `ADMIN_FIA`.
- 403 con `ADMIN_ESCUDERIA` intentando acceder a un piloto ajeno.
- 401 sin token.
- 200 en endpoint público sin información sensible.

## Observaciones de diseño

- Se reutiliza la estructura de categoria y temporada creada en la historia 1.
- Las reglas de límite se resuelven internamente con `categoria_atributo`, pero la migración V4 no debe inyectar valores por defecto; la prueba debe preparar los datos cuando lo requiera.
- El `enum` de categoria no se usa para la entidad piloto.

## Limitaciones y deuda técnica

- La migración V4 incluye las escuderías de demo `Alpine`, `Ferrari` y `McLaren`, y actualiza `usuario.escuderia_id` para `admin.escuderia` hacia Alpine.
- La relación de sanciones con `escuderia_id` sigue apuntando a `usuario`, como deuda previa del sistema.
- La creación de usuarios FIA no se incluye en la historia 2; se mantiene fuera del alcance de esta funcionalidad.
