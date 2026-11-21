package com.ocdlive.ocd.reservation.model;

import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom complet, entreprise ou marque
    @Column(name = "full_name_or_company", nullable = false)
    private String fullNameOrCompany;

    // Date et heure souhaitées pour le début du live / rendez-vous
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    // Date et heure de fin calculée selon la durée du pack réservé
    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    // Réservation en ligne ou en locale (présentiel au Togo)
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private ReservationLocationType locationType;

    // Secteur d'activité / catégorie de business (E-commerce, Musique, Corporate, etc.)
    @Column(name = "activity_sector", nullable = false, length = 150)
    private String activitySector;

    // Description du projet / activité pour le live
    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String activityDescription;

    // Pays d'où est effectuée la réservation
    @Column(nullable = false, length = 100)
    private String country;

    // Un pack peut être réservé N fois et une réservation appartient à un seul pack
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_id", nullable = false)
    private Pack pack;

    // Statut de la réservation (confirmée après paiement ou via abonnement actif)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    // Client connecté (optionnel si la réservation unitaire est faite en tant qu'invité)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_user_id")
    private User client;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
