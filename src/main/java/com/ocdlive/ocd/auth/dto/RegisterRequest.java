package com.ocdlive.ocd.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données requises pour l'inscription d'un nouvel utilisateur")
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(description = "Prénom de l'utilisateur", example = "Kofi")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(description = "Nom de l'utilisateur", example = "Mensah")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Schema(description = "Adresse email de l'utilisateur", example = "kofi@entreprise.tg")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    @Schema(description = "Mot de passe de l'utilisateur", example = "secret123")
    private String password;
}

