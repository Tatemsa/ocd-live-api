package com.ocdlive.ocd.auth.dto;

import com.ocdlive.ocd.user.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Réponse retournée après authentification réussie")
public class AuthResponse {

    @Schema(description = "Token d'accès JWT")
    private String accessToken;

    @Builder.Default
    @Schema(description = "Type de token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Durée de validité en millisecondes", example = "86400000")
    private Long expiresIn;

    @Schema(description = "Identifiant unique de l'utilisateur", example = "1")
    private Long userId;

    @Schema(description = "Adresse email de l'utilisateur", example = "admin@ocdlive.com")
    private String email;

    @Schema(description = "Prénom de l'utilisateur", example = "Super")
    private String firstName;

    @Schema(description = "Nom de l'utilisateur", example = "Admin")
    private String lastName;

    @Schema(description = "Rôle de l'utilisateur", example = "ROLE_ADMIN")
    private Role role;
}

