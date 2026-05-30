package asmacs.asmacs.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "annee_scolaire",
        uniqueConstraints = {
                // Empêche 2 fois la même année pour le même établissement
                @UniqueConstraint(
                        name        = "uk_annee_libelle_etab",
                        columnNames = {"libelle", "etablissement_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnneeScolaire {

    // Clé primaire — référencée en FK par ClasseScolaire et Eleve
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Le libellé est obligatoire")
    @Column(name = "libelle", nullable = false, length = 20)
    private String libelle; // ex: "2024-2025"

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "en_cours")
    @Builder.Default
    private Boolean enCours = false;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    // ── Clé étrangère → Etablissement ─────────────────────────────
    // Contrainte FK nommée pour retrouver facilement dans MySQL
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name           = "etablissement_id",
            nullable       = false,
            foreignKey     = @ForeignKey(name = "fk_annee_scolaire_etablissement")
    )
    private Etablissement etablissement;

    // ── Relations ──────────────────────────────────────────────────
    @OneToMany(
            mappedBy      = "anneeScolaire",
            cascade       = CascadeType.ALL,
            fetch         = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ClasseScolaire> classes = new ArrayList<>();

    @OneToMany(
            mappedBy = "anneeScolaire",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<Eleve> eleves = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}