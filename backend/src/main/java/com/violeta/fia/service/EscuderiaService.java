package com.violeta.fia.service;

import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.RolEscuderia;
import com.violeta.fia.domain.RolNombre;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.dto.EscuderiaRequest;
import com.violeta.fia.dto.EscuderiaResponse;
import com.violeta.fia.dto.MiembroEscuderiaResponse;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Reglas de gestion de escuderias: consulta del catalogo, actualizacion de la
 * informacion de una escuderia y administracion de los roles internos de sus
 * miembros. Un administrativo de escuderia solo puede operar sobre su propia
 * escuderia.
 */
@Service
public class EscuderiaService {

    private final EscuderiaRepository escuderiaRepository;
    private final UsuarioRepository usuarioRepository;

    public EscuderiaService(EscuderiaRepository escuderiaRepository,
                            UsuarioRepository usuarioRepository) {
        this.escuderiaRepository = escuderiaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<EscuderiaResponse> listar() {
        return escuderiaRepository.findAllByActivaTrueOrderByNombreAsc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public EscuderiaResponse actualizar(Long id, EscuderiaRequest request, Authentication authentication) {
        Usuario usuario = obtenerUsuario(authentication);
        Escuderia escuderia = escuderiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Escuderia no encontrada con id: " + id));

        validarPuedeActualizarInformacion(usuario, escuderia);

        escuderiaRepository.findByNombre(request.nombre())
                .filter(otra -> !Objects.equals(otra.getId(), escuderia.getId()))
                .ifPresent(otra -> {
                    throw new IllegalArgumentException("Ya existe una escuderia con ese nombre");
                });

        escuderia.setNombre(request.nombre());
        escuderia.setPais(request.pais());
        return aResponse(escuderiaRepository.save(escuderia));
    }

    @Transactional(readOnly = true)
    public List<MiembroEscuderiaResponse> listarMiembros(Authentication authentication) {
        Usuario admin = obtenerUsuario(authentication);
        Escuderia escuderia = escuderiaDelAdministrativo(admin);
        return usuarioRepository.findByEscuderiaIdOrderByApellidoAscNombreAsc(escuderia.getId()).stream()
                .map(this::aMiembroResponse)
                .toList();
    }

    @Transactional
    public MiembroEscuderiaResponse asignarRol(Long usuarioId, RolEscuderia rol, Authentication authentication) {
        Usuario admin = obtenerUsuario(authentication);
        Escuderia escuderia = escuderiaDelAdministrativo(admin);
        Usuario miembro = validarMiembroDeEscuderia(usuarioId, escuderia);

        if (rol == null) {
            throw new IllegalArgumentException("El rol de escuderia es obligatorio");
        }
        miembro.setRolEscuderia(rol);
        return aMiembroResponse(usuarioRepository.save(miembro));
    }

    @Transactional
    public MiembroEscuderiaResponse revocarRol(Long usuarioId, Authentication authentication) {
        Usuario admin = obtenerUsuario(authentication);
        Escuderia escuderia = escuderiaDelAdministrativo(admin);
        Usuario miembro = validarMiembroDeEscuderia(usuarioId, escuderia);

        miembro.setRolEscuderia(null);
        return aMiembroResponse(usuarioRepository.save(miembro));
    }

    private void validarPuedeActualizarInformacion(Usuario usuario, Escuderia escuderia) {
        if (usuario.getRol() != null && usuario.getRol().getNombre() == RolNombre.ADMIN_FIA) {
            return;
        }
        boolean pertenece = usuario.getEscuderia() != null
                && Objects.equals(usuario.getEscuderia().getId(), escuderia.getId());
        boolean rolPermite = usuario.getRolEscuderia() != null
                && usuario.getRolEscuderia().isPuedeActualizarInformacion();
        if (!pertenece || !rolPermite) {
            throw new AccessDeniedException("No tiene permisos para actualizar la informacion de esta escuderia");
        }
    }

    private Escuderia escuderiaDelAdministrativo(Usuario usuario) {
        if (usuario.getRol() == null || usuario.getRol().getNombre() != RolNombre.ADMIN_ESCUDERIA) {
            throw new AccessDeniedException("Solo el administrativo de escuderia puede gestionar roles");
        }
        if (usuario.getEscuderia() == null) {
            throw new AccessDeniedException("El administrativo de escuderia no tiene una escuderia asociada");
        }
        return usuario.getEscuderia();
    }

    private Usuario validarMiembroDeEscuderia(Long usuarioId, Escuderia escuderia) {
        Usuario miembro = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + usuarioId));
        if (miembro.getEscuderia() == null
                || !Objects.equals(miembro.getEscuderia().getId(), escuderia.getId())) {
            throw new AccessDeniedException("No puede gestionar roles de usuarios de otra escuderia");
        }
        return miembro;
    }

    private Usuario obtenerUsuario(Authentication authentication) {
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + authentication.getName()));
    }

    private EscuderiaResponse aResponse(Escuderia escuderia) {
        return new EscuderiaResponse(
                escuderia.getId(),
                escuderia.getNombre(),
                escuderia.getPais(),
                escuderia.isActiva());
    }

    private MiembroEscuderiaResponse aMiembroResponse(Usuario usuario) {
        RolEscuderia rol = usuario.getRolEscuderia();
        return new MiembroEscuderiaResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getUsername(),
                usuario.getRol() != null ? usuario.getRol().getNombre().name() : null,
                rol != null ? rol.name() : null,
                rol != null ? rol.getDescripcion() : null,
                rol != null && rol.isPuedeActualizarInformacion());
    }
}
