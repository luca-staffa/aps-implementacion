package com.violeta.fia.service;

import com.violeta.fia.domain.Usuario;
import com.violeta.fia.dto.UsuarioResponse;
import com.violeta.fia.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

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
                usuario.getUsername(),
                rol,
                usuario.isActivo(),
                usuario.getUltimaActividad());
    }
}
