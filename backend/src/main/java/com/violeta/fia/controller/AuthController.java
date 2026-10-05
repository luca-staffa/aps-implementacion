package com.violeta.fia.controller;

import com.violeta.fia.dto.LoginRequest;
import com.violeta.fia.dto.LoginResponse;
import com.violeta.fia.dto.MensajeResponse;
import com.violeta.fia.dto.RegistroRequest;
import com.violeta.fia.dto.UsuarioResponse;
import com.violeta.fia.service.AuthService;
import com.violeta.fia.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.buscarPorUsername(authentication.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<MensajeResponse> logout() {
        return ResponseEntity.ok(new MensajeResponse("Sesion finalizada"));
    }
}
