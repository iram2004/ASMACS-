package asmacs.asmacs.m02.entity;

import asmacs.asmacs.core.entity.AnneeScolaire;
import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.entity.Matiere;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_seance", nullable = false)
    private LocalDate dateSeance;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin")
    private LocalTime heureFin;

    // COURS | EXAMEN | TD | TP
    @Column(name = "type_seance", length = 20)
    @Builder.Default
    private String typeSeance = "COURS";

    // PLANIFIEE | EN_COURS | TERMINEE | ANNULEE
    @Column(name = "statut", length = 20)
    @Builder.Default
    private String statut = "PLANIFIEE";

    @Column(name = "salle", length = 30)
    private String salle;

    // Coordonnées GPS autorisées pour cette séance
    @Column(name = "latitude_autorisee")
    private Double latitudeAutorisee;

    @Column(name = "longitude_autorisee")
    private Double longitudeAutorisee;

    @Column(name = "rayon_autorisation_metres")
    @Builder.Default
    private Integer rayonAutorisationMetres = 200;

    @Column(name = "geofencing_actif")
    @Builder.Default
    private Boolean geofencingActif = false;

    @Column(name = "qr_code_token", unique = true, length = 100)
    private String qrCodeToken;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    // ── FK → Classe ────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "classe_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_seance_classe")
    )
    private ClasseScolaire classe;

    // ── FK → Matière ───────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "matiere_id",
            foreignKey = @ForeignKey(name = "fk_seance_matiere")
    )
    private Matiere matiere;

    // ── FK → Enseignant ────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "enseignant_id",
            foreignKey = @ForeignKey(name = "fk_seance_enseignant")
    )
    private Enseignant enseignant;

    // ── FK → AnneeScolaire ─────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "annee_scolaire_id",
            foreignKey = @ForeignKey(name = "fk_seance_annee")
    )
    private AnneeScolaire anneeScolaire;

    // ── Présences de cette séance ──────────────────────────
    @OneToMany(
            mappedBy = "seance",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Presence> presences = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}