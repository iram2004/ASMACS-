package asmacs.asmacs.core.entity;

import asmacs.asmacs.core.enums.NiveauEnseignement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classe_scolaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasseScolaire {

    // Clé primaire — référencée en FK par Eleve
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Le code est obligatoire")
    @Column(name = "code", nullable = false, length = 20)
    private String code; // ex: "6A", "TleC", "L1Info"

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false, length = 80)
    private String nom; // ex: "Sixième A"

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_enseignement", nullable = false)
    private NiveauEnseignement niveauEnseignement;

    @Column(name = "filiere", length = 50)
    private String filiere; // ex: C, D, A4, Sciences

    @Column(name = "capacite_max")
    @Builder.Default
    private Integer capaciteMax = 60;

    @Column(name = "salle", length = 30)
    private String salle;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    // ── Clé étrangère → AnneeScolaire ─────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name       = "annee_scolaire_id",
            nullable   = false,
            foreignKey = @ForeignKey(name = "fk_classe_annee_scolaire")
    )
    private AnneeScolaire anneeScolaire;

    // ── Clé étrangère → Etablissement ─────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name       = "etablissement_id",
            nullable   = false,
            foreignKey = @ForeignKey(name = "fk_classe_etablissement")
    )
    private Etablissement etablissement;

    // ── Clé étrangère → Enseignant principal ──────────────────────
    // Nullable : une classe peut ne pas encore avoir d'enseignant principal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name       = "enseignant_principal_id",
            foreignKey = @ForeignKey(name = "fk_classe_enseignant_principal")
    )
    private Enseignant enseignantPrincipal;

    // ── Liste des élèves dans cette classe ─────────────────────────
    @OneToMany(
            mappedBy = "classeScolaire",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<Eleve> eleves = new ArrayList<>();

    // ── Table pivot classe_matiere ─────────────────────────────────
    // Crée automatiquement la table de jointure avec 2 FK nommées
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name               = "classe_matiere",
            joinColumns        = @JoinColumn(
                    name           = "classe_id",
                    foreignKey     = @ForeignKey(name = "fk_cm_classe")
            ),
            inverseJoinColumns = @JoinColumn(
                    name           = "matiere_id",
                    foreignKey     = @ForeignKey(name = "fk_cm_matiere")
            )
    )
    @Builder.Default
    private List<Matiere> matieres = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}