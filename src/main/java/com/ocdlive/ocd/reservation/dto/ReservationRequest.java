package com.ocdlive.ocd.reservation.dto;

import com.ocdlive.ocd.reservation.model.ReservationLocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Données nécessaires pour effectuer une réservation de pack")
public class ReservationRequest {

    @NotBlank(message = "Le nom complet, nom de l'entreprise ou marque est obligatoire")
    @Schema(description = "Nom complet de la personne, marque ou entreprise", example = "Agence Digitale Nova")
    private String fullNameOrCompany;

    @NotNull(message = "La date et l'heure de la réservation sont obligatoires")
    @Future(message = "La date et l'heure du rendez-vous doivent être dans le futur")
    @Schema(description = "Date et heure souhaitées pour le live", example = "2026-12-15T14:30:00")
    private LocalDateTime appointmentDateTime;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Schema(description = "Numéro de téléphone de contact", example = "+237699000111")
    private String phoneNumber;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Schema(description = "Adresse email de confirmation", example = "contact@nova-agency.com")
    private String email;

    @NotNull(message = "Le type de lieu (ONLINE ou LOCAL) est obligatoire")
    @Schema(description = "Modalité du live : ONLINE (en ligne) ou LOCAL (en présentiel/studio)", example = "LOCAL")
    private ReservationLocationType locationType;

    @NotNull(message = "Le pack sélectionné est obligatoire")
    @Schema(description = "Identifiant du pack sélectionné", example = "1")
    private Long packId;

    @NotBlank(message = "Le secteur d'activité est obligatoire")
    @Schema(description = "Secteur d'activité", example = "E-commerce & Mode")
    private String activitySector;

    @Schema(description = "Description du projet ou déroulement souhaité du live", example = "Lancement de notre nouvelle collection de prêt-à-porter avec défilé interactif et vente en direct")
    private String activityDescription;

    @NotBlank(message = "Le pays est obligatoire")
    @Schema(description = "Pays depuis lequel la réservation est effectuée", example = "Cameroun")
    private String country;
}

