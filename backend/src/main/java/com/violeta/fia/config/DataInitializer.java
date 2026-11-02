package com.violeta.fia.config;

import com.violeta.fia.domain.Escuderia;
import com.violeta.fia.domain.Rol;
import com.violeta.fia.domain.RolEscuderia;
import com.violeta.fia.domain.RolNombre;
import com.violeta.fia.domain.Usuario;
import com.violeta.fia.repository.EscuderiaRepository;
import com.violeta.fia.repository.RolRepository;
import com.violeta.fia.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final String PASSWORD_INICIAL = "Password123!";

    /**
     * Garantiza que existan los roles y las credenciales iniciales con contrasenas
     * correctamente hasheadas, independientemente del contenido de la semilla SQL.
     * La referencia de categorias y temporadas se carga exclusivamente desde Flyway V2.
     */
    @Bean
    CommandLineRunner inicializarDatos(UsuarioRepository usuarioRepository,
                                       RolRepository rolRepository,
                                       EscuderiaRepository escuderiaRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            Rol adminFia = asegurarRol(rolRepository, RolNombre.ADMIN_FIA, "Administrador general de la FIA");
            Rol adminEscuderia = asegurarRol(rolRepository, RolNombre.ADMIN_ESCUDERIA, "Administrador de escuderia");
            Rol publico = asegurarRol(rolRepository, RolNombre.PUBLICO, "Usuario publico");

            Escuderia alpine = asegurarEscuderia(escuderiaRepository, "Alpine", "Francia");
            Escuderia ferrari = asegurarEscuderia(escuderiaRepository, "Ferrari", "Italia");
            Escuderia mclaren = asegurarEscuderia(escuderiaRepository, "McLaren", "Reino Unido");

            asegurarUsuario(usuarioRepository, passwordEncoder, "Admin", "FIA",
                    "admin@fia.com", "admin.fia", adminFia, null, null);
            asegurarUsuario(usuarioRepository, passwordEncoder, "Admin", "Escuderia",
                    "escuderia@fia.com", "admin.escuderia", adminEscuderia, alpine, RolEscuderia.ADMINISTRADOR);
            asegurarUsuario(usuarioRepository, passwordEncoder, "Usuario", "Publico",
                    "publico@fia.com", "publico", publico, null, null);
            asegurarUsuario(usuarioRepository, passwordEncoder, "Piloto", "Demo",
                    "piloto@fia.com", "piloto.demo", publico, ferrari, null);

            asegurarUsuario(usuarioRepository, passwordEncoder, "Escuderia", "Ferrari",
                    "ferrari@fia.com", "admin.ferrari", adminEscuderia, ferrari, RolEscuderia.ADMINISTRADOR);
            asegurarUsuario(usuarioRepository, passwordEncoder, "Escuderia", "McLaren",
                    "mclaren@fia.com", "admin.mclaren", adminEscuderia, mclaren, RolEscuderia.ADMINISTRADOR);

            asegurarUsuario(usuarioRepository, passwordEncoder, "Editor", "Alpine",
                    "editor.alpine@fia.com", "editor.alpine", publico, alpine, RolEscuderia.EDITOR);
            asegurarUsuario(usuarioRepository, passwordEncoder, "Lector", "Alpine",
                    "lector.alpine@fia.com", "lector.alpine", publico, alpine, RolEscuderia.LECTOR);
            asegurarUsuario(usuarioRepository, passwordEncoder, "Editor", "Ferrari",
                    "editor.ferrari@fia.com", "editor.ferrari", publico, ferrari, RolEscuderia.EDITOR);
        };
    }

    private Rol asegurarRol(RolRepository rolRepository, RolNombre nombre, String descripcion) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            return rolRepository.save(rol);
        });
    }

    private Escuderia asegurarEscuderia(EscuderiaRepository escuderiaRepository, String nombre, String pais) {
        return escuderiaRepository.findByNombre(nombre).orElseGet(() -> escuderiaRepository.save(new Escuderia(nombre, pais, true)));
    }

    private void asegurarUsuario(UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder,
                                 String nombre, String apellido,
                                 String email, String username, Rol rol, Escuderia escuderia,
                                 RolEscuderia rolEscuderia) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElseGet(Usuario::new);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(PASSWORD_INICIAL));
        usuario.setRol(rol);
        usuario.setEscuderia(escuderia);
        usuario.setRolEscuderia(rolEscuderia);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}
