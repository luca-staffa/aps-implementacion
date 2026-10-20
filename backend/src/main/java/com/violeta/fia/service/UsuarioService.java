package com.violeta.fia.service;

import com.violeta.fia.domain.Usuario;
import com.violeta.fia.dto.ActualizarDatosRequest;
import com.violeta.fia.dto.UsuarioResponse;
import com.violeta.fia.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::aResponse).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return aResponse(buscarEntidad(id));
    }

    public UsuarioResponse buscarPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + username));
        return aResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarDatos(String username, ActualizarDatosRequest request) {
        Usuario usuario = buscarEntidadPorUsername(username);

        usuarioRepository.findByEmail(request.email())
                .filter(otro -> !Objects.equals(otro.getId(), usuario.getId()))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException("El email ya esta registrado");
                });

        usuario.setEmail(request.email());
        usuario.setTelefono(request.telefono());
        return aResponse(usuarioRepository.save(usuario));
    }

    public Usuario buscarEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));
    }

    public Usuario buscarEntidadPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + username));
    }

    public UsuarioResponse aResponse(Usuario usuario) {
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre().name() : null;
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getUsername(),
                rol,
                usuario.isActivo(),
                usuario.getUltimaActividad());
    }
}
