package com.ocdlive.ocd.timeslot.model;

import com.ocdlive.ocd.pack.model.Pack;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "country_time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pays auquel ce créneau horaire est dédié (ex: "Cameroun")
    @Column(nullable = false, length = 100)
    private String country;

    // Jour de la semaine (optionnel : si null, s'applique tous les jours)
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 20)
    private DayOfWeek dayOfWeek;

    // Heure de début du créneau dédié (ex: 08:00)
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    // Heure de fin du créneau dédié (ex: 10:00)
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // Indique si un abonnement mensuel est requis pour réserver sur ce créneau
    @Builder.Default
    @Column(name = "subscription_required", nullable = false)
    private boolean subscriptionRequired = true;

    // Pack spécifique associé à ce créneau (optionnel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_id")
    private Pack pack;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

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

