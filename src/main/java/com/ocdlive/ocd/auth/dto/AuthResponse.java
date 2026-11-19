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
@Schema(description = "Réponse retournée après authentification ou rafraîchissement de session")
public class AuthResponse {

    @Schema(description = "Token d'accès JWT")
    private String accessToken;

    @Schema(description = "Token de rafraîchissement JWT (Refresh Token)")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Type de token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Durée de validité de l'access token en millisecondes", example = "86400000")
    private Long expiresIn;

    @Schema(description = "Durée de validité du refresh token en millisecondes", example = "604800000")
    private Long refreshExpiresIn;

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
