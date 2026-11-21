package com.ocdlive.ocd.auth.controller;

import com.ocdlive.ocd.auth.dto.AuthResponse;
import com.ocdlive.ocd.auth.dto.LoginRequest;
import com.ocdlive.ocd.auth.dto.RegisterRequest;
import com.ocdlive.ocd.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints d'authentification, d'inscription et gestion de session")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur", description = "Crée un compte client et retourne un token JWT Bearer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compte créé avec succès, token retourné"),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
            @ApiResponse(responseCode = "409", description = "Un compte existe déjà avec cet email")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur", description = "Valide les identifiants et retourne un token JWT Bearer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie, token retourné"),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides ou compte désactivé")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
