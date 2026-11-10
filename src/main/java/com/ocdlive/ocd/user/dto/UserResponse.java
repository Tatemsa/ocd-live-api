package com.ocdlive.ocd.user.dto;

import com.ocdlive.ocd.user.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Informations publiques d'un utilisateur")
public class UserResponse {

    @Schema(description = "Identifiant de l'utilisateur", example = "1")
    private Long id;

    @Schema(description = "Adresse email", example = "admin@ocdlive.com")
    private String email;

    @Schema(description = "Prénom", example = "Super")
    private String firstName;

    @Schema(description = "Nom", example = "Admin")
    private String lastName;

    @Schema(description = "Rôle de l'utilisateur", example = "ROLE_ADMIN")
    private Role role;

    @Schema(description = "Compte activé", example = "true")
    private boolean enabled;

    @Schema(description = "Date de création du compte")
    private LocalDateTime createdAt;
}

