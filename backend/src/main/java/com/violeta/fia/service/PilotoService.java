package com.violeta.fia.service;

import com.violeta.fia.domain.CategoriaAtributo;
import com.violeta.fia.domain.CategoriaCatalogo;
import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.Piloto;
import com.violeta.fia.domain.RolNombre;
import com.violeta.fia.domain.Temporada;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.dto.PilotoPublicResponse;
import com.violeta.fia.dto.PilotoRequest;
import com.violeta.fia.dto.PilotoResponse;
import com.violeta.fia.repository.CategoriaAtributoRepository;
import com.violeta.fia.repository.CategoriaCatalogoRepository;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.PilotoRepository;
import com.violeta.fia.repository.TemporadaRepository;
import com.violeta.fia.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PilotoService {

    private final PilotoRepository pilotoRepository;
    private final CategoriaCatalogoRepository categoriaCatalogoRepository;
    private final TemporadaRepository temporadaRepository;
    private final EscuderiaRepository escuderiaRepository;
    private final CategoriaAtributoRepository categoriaAtributoRepository;
    private final UsuarioRepository usuarioRepository;

    public PilotoService(PilotoRepository pilotoRepository,
                         CategoriaCatalogoRepository categoriaCatalogoRepository,
                         TemporadaRepository temporadaRepository,
                         EscuderiaRepository escuderiaRepository,
                         CategoriaAtributoRepository categoriaAtributoRepository,
                         UsuarioRepository usuarioRepository) {
        this.pilotoRepository = pilotoRepository;
        this.categoriaCatalogoRepository = categoriaCatalogoRepository;
        this.temporadaRepository = temporadaRepository;
        this.escuderiaRepository = escuderiaRepository;
        this.categoriaAtributoRepository = categoriaAtributoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PilotoResponse> listar(Authentication authentication,
                                       Long categoriaId, String categoria,
                                       Long temporadaId, Integer temporada) {
        Usuario usuario = obtenerUsuario(authentication);
        List<Piloto> pilotos;

        if (usuario.getRol() != null && usuario.getRol().getNombre() == RolNombre.ADMIN_ESCUDERIA) {
            if (usuario.getEscuderia() == null) {
                throw new AccessDeniedException("El administrador de escuderia no tiene una escuderia asociada");
            }
            pilotos = pilotoRepository.findByActivoTrueAndEscuderiaIdOrderByApellidoAsc(usuario.getEscuderia().getId());
        } else {
            pilotos = pilotoRepository.findAllByActivoTrueOrderByApellidoAsc();
        }

        Long filtroCategoriaId = resolverCategoriaId(categoriaId, categoria);
        Long filtroTemporadaId = resolverTemporadaId(temporadaId, temporada);

        return pilotos.stream()
                .filter(piloto -> filtroCategoriaId == null
                        || Objects.equals(piloto.getCategoria().getId(), filtroCategoriaId))
                .filter(piloto -> filtroTemporadaId == null
                        || Objects.equals(piloto.getTemporada().getId(), filtroTemporadaId))
                .map(this::toResponse)
                .toList();
    }

    private Long resolverCategoriaId(Long categoriaId, String categoriaCodigo) {
        if (categoriaId != null) {
            return categoriaId;
        }
        if (categoriaCodigo == null || categoriaCodigo.isBlank()) {
            return null;
        }
        return categoriaCatalogoRepository.findByCodigo(categoriaCodigo.trim())
                .map(CategoriaCatalogo::getId)
                .orElse(-1L);
    }

    private Long resolverTemporadaId(Long temporadaId, Integer anio) {
        if (temporadaId != null) {
            return temporadaId;
        }
        if (anio == null) {
            return null;
        }
        return temporadaRepository.findByAnio(anio)
                .map(Temporada::getId)
                .orElse(-1L);
    }

    @Transactional(readOnly = true)
    public PilotoResponse obtener(Long id, Authentication authentication) {
        Piloto piloto = pilotoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Piloto no encontrado con id: " + id));
        validarAcceso(piloto, obtenerUsuario(authentication));
        return toResponse(piloto);
    }

    @Transactional
    public PilotoResponse crear(PilotoRequest request, Authentication authentication) {
        Usuario usuario = obtenerUsuario(authentication);
        validarRolEscritura(usuario);
        validar(request, null);

        CategoriaCatalogo categoria = categoriaCatalogoRepository.findById(request.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + request.categoriaId()));
        Temporada temporada = temporadaRepository.findById(request.temporadaId())
                .orElseThrow(() -> new IllegalArgumentException("Temporada no encontrada con id: " + request.temporadaId()));

        Escuderia escuderia = determinarEscuderia(request, usuario);
        validarLimiteEscuderia(categoria, temporada, escuderia, request.rol(), null);

        Piloto piloto = new Piloto();
        aplicarCambios(piloto, request, categoria, temporada, escuderia);
        return toResponse(pilotoRepository.save(piloto));
    }

    @Transactional
    public PilotoResponse actualizar(Long id, PilotoRequest request, Authentication authentication) {
        Piloto piloto = pilotoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Piloto no encontrado con id: " + id));
        Usuario usuario = obtenerUsuario(authentication);
        validarRolEscritura(usuario);
        validarAcceso(piloto, usuario);
        validar(request, id);

        CategoriaCatalogo categoria = categoriaCatalogoRepository.findById(request.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + request.categoriaId()));
        Temporada temporada = temporadaRepository.findById(request.temporadaId())
                .orElseThrow(() -> new IllegalArgumentException("Temporada no encontrada con id: " + request.temporadaId()));

        Escuderia escuderia = determinarEscuderia(request, usuario);
        validarLimiteEscuderia(categoria, temporada, escuderia, request.rol(), id);
        aplicarCambios(piloto, request, categoria, temporada, escuderia);
        piloto.setFechaModificacion(LocalDateTime.now());
        return toResponse(pilotoRepository.save(piloto));
    }

    @Transactional
    public void eliminar(Long id, Authentication authentication) {
        Piloto piloto = pilotoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Piloto no encontrado con id: " + id));
        Usuario usuario = obtenerUsuario(authentication);
        validarRolEscritura(usuario);
        validarAcceso(piloto, usuario);
        piloto.setActivo(false);
        piloto.setFechaModificacion(LocalDateTime.now());
        pilotoRepository.save(piloto);
    }

    @Transactional(readOnly = true)
    public List<PilotoPublicResponse> listarPublico(Long categoriaId, Long temporadaId, Long escuderiaId, String escuderia) {
        String nombreEscuderia = escuderia != null ? escuderia.trim().toLowerCase() : null;
        return pilotoRepository.findAllByActivoTrueOrderByApellidoAsc().stream()
                .filter(piloto -> categoriaId == null || Objects.equals(piloto.getCategoria().getId(), categoriaId))
                .filter(piloto -> temporadaId == null || Objects.equals(piloto.getTemporada().getId(), temporadaId))
                .filter(piloto -> escuderiaId == null || Objects.equals(piloto.getEscuderia().getId(), escuderiaId))
                .filter(piloto -> nombreEscuderia == null || nombreEscuderia.isEmpty()
                        || (piloto.getEscuderia() != null && piloto.getEscuderia().getNombre() != null
                        && piloto.getEscuderia().getNombre().toLowerCase().contains(nombreEscuderia)))
                .map(this::toPublicResponse)
                .toList();
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + authentication.getName()));
    }

    private void validarRolEscritura(Usuario usuario) {
        if (usuario.getRol() != null && usuario.getRol().getNombre() == RolNombre.ADMIN_FIA) {
            throw new AccessDeniedException("El administrador FIA solo puede consultar pilotos");
        }
    }

    private void validarAcceso(Piloto piloto, Usuario usuario) {
        if (usuario.getRol() != null && usuario.getRol().getNombre() == RolNombre.ADMIN_ESCUDERIA) {
            if (usuario.getEscuderia() == null) {
                throw new AccessDeniedException("El administrador de escuderia no tiene una escuderia asociada");
            }
            if (!Objects.equals(piloto.getEscuderia().getId(), usuario.getEscuderia().getId())) {
                throw new AccessDeniedException("No tiene permisos para operar sobre pilotos de otra escuderia");
            }
        }
    }

    private Escuderia determinarEscuderia(PilotoRequest request, Usuario usuario) {
        if (usuario.getRol() != null && usuario.getRol().getNombre() == RolNombre.ADMIN_ESCUDERIA) {
            if (usuario.getEscuderia() == null) {
                throw new AccessDeniedException("El administrador de escuderia no tiene una escuderia asociada");
            }
            return usuario.getEscuderia();
        }

        if (request.escuderiaId() == null) {
            throw new IllegalArgumentException("La escuderia es obligatoria");
        }

        return escuderiaRepository.findById(request.escuderiaId())
                .orElseThrow(() -> new IllegalArgumentException("Escuderia no encontrada con id: " + request.escuderiaId()));
    }

    private void validar(PilotoRequest request, Long pilotoIdActual) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (request.apellido() == null || request.apellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (request.fechaNacimiento() == null || !request.fechaNacimiento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento debe estar en el pasado");
        }
        if (request.nacionalidad() == null || request.nacionalidad().isBlank()) {
            throw new IllegalArgumentException("La nacionalidad es obligatoria");
        }
        if (request.numero() == null || request.numero() <= 0) {
            throw new IllegalArgumentException("El numero debe ser positivo");
        }
        if (request.abreviatura() == null || !request.abreviatura().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("La abreviatura debe tener exactamente 3 letras mayusculas");
        }
        if (request.rol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        if (request.numeroLicenciaFia() == null || request.numeroLicenciaFia().isBlank()) {
            throw new IllegalArgumentException("El numero de licencia FIA es obligatorio");
        }

        Optional<Piloto> existenteNumero = pilotoRepository.findByCategoriaIdAndTemporadaIdAndNumero(request.categoriaId(), request.temporadaId(), request.numero());
        if (existenteNumero.isPresent() && !Objects.equals(existenteNumero.get().getId(), pilotoIdActual)) {
            throw new IllegalArgumentException("Ya existe un piloto con ese numero para la categoria y temporada");
        }

        Optional<Piloto> existenteAbreviatura = pilotoRepository.findByCategoriaIdAndTemporadaIdAndAbreviaturaIgnoreCase(request.categoriaId(), request.temporadaId(), request.abreviatura());
        if (existenteAbreviatura.isPresent() && !Objects.equals(existenteAbreviatura.get().getId(), pilotoIdActual)) {
            throw new IllegalArgumentException("Ya existe un piloto con esa abreviatura para la categoria y temporada");
        }
    }

    private void validarLimiteEscuderia(CategoriaCatalogo categoria, Temporada temporada, Escuderia escuderia,
                                       Piloto.RolPiloto rol, Long pilotoIdActual) {
        String clave = rol == Piloto.RolPiloto.TITULAR ? "max_titulares" : "max_suplentes";
        Integer maximo = buscarLimiteCategoria(categoria.getId(), clave);
        if (maximo == null) {
            return;
        }

        long cantidad = pilotoRepository.findByEscuderiaIdAndActivoTrueAndCategoriaIdAndTemporadaId(
                        escuderia.getId(), categoria.getId(), temporada.getId()).stream()
                .filter(piloto -> piloto.getRol() == rol)
                .filter(piloto -> !Objects.equals(piloto.getId(), pilotoIdActual))
                .count();

        if (cantidad >= maximo) {
            throw new IllegalArgumentException("Se excedio el limite de " + (rol == Piloto.RolPiloto.TITULAR ? "titulares" : "suplentes") + " para esta escuderia");
        }
    }

    private Integer buscarLimiteCategoria(Long categoriaId, String clave) {
        List<CategoriaAtributo> atributos = categoriaAtributoRepository.findByCategoriaIdOrderByClaveAsc(categoriaId);
        for (CategoriaAtributo atributo : atributos) {
            if (clave.equalsIgnoreCase(atributo.getClave())) {
                try {
                    return Integer.parseInt(atributo.getValor());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private void aplicarCambios(Piloto piloto, PilotoRequest request, CategoriaCatalogo categoria,
                                Temporada temporada, Escuderia escuderia) {
        piloto.setNombre(request.nombre());
        piloto.setApellido(request.apellido());
        piloto.setFechaNacimiento(request.fechaNacimiento());
        piloto.setNacionalidad(request.nacionalidad());
        piloto.setNumero(request.numero());
        piloto.setAbreviatura(request.abreviatura().toUpperCase());
        piloto.setRol(request.rol());
        piloto.setNumeroLicenciaFia(request.numeroLicenciaFia());
        piloto.setFotoUrl(request.fotoUrl());
        piloto.setActivo(true);
        piloto.setCategoria(categoria);
        piloto.setTemporada(temporada);
        piloto.setEscuderia(escuderia);
        piloto.setFechaModificacion(LocalDateTime.now());
    }

    private PilotoResponse toResponse(Piloto piloto) {
        return new PilotoResponse(
                piloto.getId(),
                piloto.getNombre(),
                piloto.getApellido(),
                piloto.getFechaNacimiento() != null ? piloto.getFechaNacimiento().toString() : null,
                piloto.getNacionalidad(),
                piloto.getNumero(),
                piloto.getAbreviatura(),
                piloto.getRol() != null ? piloto.getRol().name() : null,
                piloto.getNumeroLicenciaFia(),
                piloto.getFotoUrl(),
                piloto.isActivo(),
                piloto.getCategoria() != null ? piloto.getCategoria().getId() : null,
                piloto.getCategoria() != null ? piloto.getCategoria().getCodigo() : null,
                piloto.getTemporada() != null ? piloto.getTemporada().getId() : null,
                piloto.getTemporada() != null ? piloto.getTemporada().getAnio() : null,
                piloto.getEscuderia() != null ? piloto.getEscuderia().getId() : null,
                piloto.getEscuderia() != null ? piloto.getEscuderia().getNombre() : null
        );
    }

    private PilotoPublicResponse toPublicResponse(Piloto piloto) {
        return new PilotoPublicResponse(
                piloto.getId(),
                piloto.getNombre(),
                piloto.getApellido(),
                piloto.getNacionalidad(),
                piloto.getNumero(),
                piloto.getAbreviatura(),
                piloto.getRol() != null ? piloto.getRol().name() : null,
                piloto.getFotoUrl(),
                piloto.getCategoria() != null ? piloto.getCategoria().getId() : null,
                piloto.getCategoria() != null ? piloto.getCategoria().getCodigo() : null,
                piloto.getTemporada() != null ? piloto.getTemporada().getId() : null,
                piloto.getTemporada() != null ? piloto.getTemporada().getAnio() : null,
                piloto.getEscuderia() != null ? piloto.getEscuderia().getId() : null,
                piloto.getEscuderia() != null ? piloto.getEscuderia().getNombre() : null
        );
    }
}
