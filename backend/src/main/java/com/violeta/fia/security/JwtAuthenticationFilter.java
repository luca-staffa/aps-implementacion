package com.violeta.fia.security;

import com.violeta.fia.domain.Usuario;
import com.violeta.fia.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final CustomUserDetailsService userDetailsService;
    private final long inactivityTimeout;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UsuarioRepository usuarioRepository,
                                   CustomUserDetailsService userDetailsService,
                                   @Value("${app.jwt.inactivity-timeout}") long inactivityTimeout) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.userDetailsService = userDetailsService;
        this.inactivityTimeout = inactivityTimeout;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            String username = jwtService.extraerUsername(token);
            Authentication autenticacionActual = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && autenticacionActual == null) {
                Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
                if (usuario == null || !usuario.isActivo()) {
                    filterChain.doFilter(request, response);
                    return;
                }

                LocalDateTime ahora = LocalDateTime.now();
                LocalDateTime ultimaActividad = usuario.getUltimaActividad();
                if (ultimaActividad != null
                        && Duration.between(ultimaActividad, ahora).toMillis() > inactivityTimeout) {
                    escribirNoAutorizado(response, "Sesion expirada por inactividad");
                    return;
                }

                usuario.setUltimaActividad(ahora);
                usuarioRepository.save(usuario);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.esValido(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void escribirNoAutorizado(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401,\"error\":\"No autorizado\",\"mensaje\":\"" + mensaje + "\"}");
    }
}
