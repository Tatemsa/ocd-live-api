package com.ocdlive.ocd.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Requête permettant de renouveler le token d'accès à partir d'un refresh token")
public class RefreshTokenRequest {

    @NotBlank(message = "Le refresh token est obligatoire")
    @Schema(description = "Token de rafraîchissement JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}

