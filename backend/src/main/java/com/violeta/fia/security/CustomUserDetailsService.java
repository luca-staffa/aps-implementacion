package com.violeta.fia.security;

import com.violeta.fia.domain.Usuario;
import com.violeta.fia.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        String rol = usuario.getRol() != null ? usuario.getRol().getNombre().name() : "PUBLICO";

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities("ROLE_" + rol)
                .disabled(!usuario.isActivo())
                .build();
    }
}
