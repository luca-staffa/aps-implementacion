package com.violeta.fia.service;

import com.violeta.fia.domain.Rol;
import com.violeta.fia.domain.RolNombre;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.dto.LoginRequest;
import com.violeta.fia.dto.LoginResponse;
import com.violeta.fia.dto.RegistroRequest;
import com.violeta.fia.dto.UsuarioResponse;
import com.violeta.fia.repository.RolRepository;
import com.violeta.fia.repository.UsuarioRepository;
import com.violeta.fia.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya esta en uso");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }

        Rol rolPublico = rolRepository.findByNombre(RolNombre.PUBLICO)
                .orElseThrow(() -> new NoSuchElementException("El rol PUBLICO no esta configurado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(request.email());
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(rolPublico);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        return usuarioService.aResponse(usuarioRepository.save(usuario));
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        usuario.setUltimaActividad(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String rol = usuario.getRol() != null ? usuario.getRol().getNombre().name() : "PUBLICO";
        String token = jwtService.generarToken(usuario.getUsername(), rol);

        return new LoginResponse(token, "Bearer", usuario.getUsername(), rol, jwtService.getExpiracion());
    }
}
