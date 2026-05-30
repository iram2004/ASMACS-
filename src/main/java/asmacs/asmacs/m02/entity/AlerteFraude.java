package asmacs.asmacs.m02.entity;

import asmacs.asmacs.core.entity.Eleve;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerte_fraude")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteFraude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GPS_INVALIDE | QR_DOUBLE | FACIAL_ECHEC | NFC_INVALIDE
    @Column(name = "type_alerte", nullable = false, length = 30)
    private String typeAlerte;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "latitude_tentative")
    private Double latitudeTentative;

    @Column(name = "longitude_tentative")
    private Double longitudeTentative;

    // NOUVELLE | TRAITEE | IGNOREE
    @Column(name = "statut", length = 20)
    @Builder.Default
    private String statut = "NOUVELLE";

    @Column(name = "date_alerte", updatable = false)
    private LocalDateTime dateAlerte;

    // ── FK → Eleve ─────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "eleve_id",
            foreignKey = @ForeignKey(name = "fk_alerte_eleve")
    )
    private Eleve eleve;

    // ── FK → Seance ────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "seance_id",
            foreignKey = @ForeignKey(name = "fk_alerte_seance")
    )
    private Seance seance;

    @PrePersist
    protected void onCreate() {
        this.dateAlerte = LocalDateTime.now();
    }
}