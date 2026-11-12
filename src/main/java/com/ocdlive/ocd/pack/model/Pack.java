package com.ocdlive.ocd.pack.model;

import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type", nullable = false, length = 30)
    @Builder.Default
    private PackBillingType billingType = PackBillingType.ONE_TIME;

    // Pays cible si ce pack est dédié aux créneaux d'un pays particulier (ex: Cameroun)
    @Column(name = "target_country", length = 100)
    private String targetCountry;

    // Nombre de lives inclus par mois (pertinent pour un pack MONTHLY_SUBSCRIPTION)
    @Column(name = "monthly_live_quota")
    private Integer monthlyLiveQuota;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pack_equipments",
            joinColumns = @JoinColumn(name = "pack_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    @Builder.Default
    private List<Equipment> equipments = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "pack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.ocdlive.ocd.reservation.model.Reservation> reservations = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.billingType == null) {
            this.billingType = PackBillingType.ONE_TIME;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
