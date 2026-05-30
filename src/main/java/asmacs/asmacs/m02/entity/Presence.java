package asmacs.asmacs.m02.entity;

import asmacs.asmacs.core.entity.Eleve;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "presence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_presence_eleve_seance",
                        columnNames = {"eleve_id", "seance_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PRESENT | ABSENT | RETARD | EXCUSED
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private String statut = "ABSENT";

    // QR_CODE | FACIAL | MANUEL | NFC
    @Column(name = "methode_validation", length = 20)
    private String methodeValidation;

    @Column(name = "heure_arrivee")
    private LocalDateTime heureArrivee;

    @Column(name = "motif_absence", length = 300)
    private String motifAbsence;

    @Column(name = "justifiee")
    @Builder.Default
    private Boolean justifiee = false;

    // Coordonnées GPS au moment du pointage
    @Column(name = "latitude_pointage")
    private Double latitudePointage;

    @Column(name = "longitude_pointage")
    private Double longitudePointage;

    @Column(name = "fraude_detectee")
    @Builder.Default
    private Boolean fraudeDetectee = false;

    @Column(name = "score_facial")
    private Double scoreFacial;

    @Column(name = "note_observation", length = 300)
    private String noteObservation;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // ── FK → Eleve ─────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "eleve_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_presence_eleve")
    )
    private Eleve eleve;

    // ── FK → Seance ────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seance_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_presence_seance")
    )
    private Seance seance;

    @PrePersist
    protected void onCreate() {
        this.dateCreation    = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}