package asmacs.asmacs.core.entity;

import asmacs.asmacs.core.enums.Sexe;
import asmacs.asmacs.core.enums.TypeEnseignant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "enseignant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_enseignant_matricule", columnNames = "matricule")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enseignant {

    // Clé primaire — référencée en FK par ClasseScolaire (enseignant_principal_id)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "matricule", unique = true, length = 30)
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false, length = 80)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false, length = 80)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexe")
    private Sexe sexe;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "specialite", length = 100)
    private String specialite;

    @Column(name = "grade", length = 80)
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_enseignant")
    private TypeEnseignant typeEnseignant;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "salaire_base")
    private Double salaireBase;

    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    // ── Clé étrangère → Etablissement ─────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name       = "etablissement_id",
            nullable   = false,
            foreignKey = @ForeignKey(name = "fk_enseignant_etablissement")
    )
    private Etablissement etablissement;

    // ── Clé étrangère → Utilisateur (compte de connexion) ─────────
    // Un enseignant a un et un seul compte utilisateur
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name       = "utilisateur_id",
            foreignKey = @ForeignKey(name = "fk_enseignant_utilisateur")
    )
    private Utilisateur utilisateur;

    // ── Table pivot enseignant_matiere ─────────────────────────────
    // Crée automatiquement la table de jointure avec 2 FK nommées
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name               = "enseignant_matiere",
            joinColumns        = @JoinColumn(
                    name           = "enseignant_id",
                    foreignKey     = @ForeignKey(name = "fk_em_enseignant")
            ),
            inverseJoinColumns = @JoinColumn(
                    name           = "matiere_id",
                    foreignKey     = @ForeignKey(name = "fk_em_matiere")
            )
    )
    @Builder.Default
    private List<Matiere> matieres = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}