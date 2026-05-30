package asmacs.asmacs.core.entity;

import asmacs.asmacs.core.enums.NiveauEnseignement;
import asmacs.asmacs.core.enums.Sexe;
import asmacs.asmacs.core.enums.StatutEleve;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "eleve",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_eleve_matricule", columnNames = "matricule")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Eleve {

    // Clé primaire — référencée en FK par toutes les entités modules :
    // Evaluation (M03), Presence (M05), Candidature (M06),
    // DecisionCursus (M07), Emprunt (M08), Facture (M09)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "matricule", unique = true, length = 30)
    private String matricule; // Généré automatiquement par M01

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

    @Column(name = "lieu_naissance", length = 100)
    private String lieuNaissance;

    @Column(name = "nationalite", length = 50)
    @Builder.Default
    private String nationalite = "Camerounaise";

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 120)
    private String email;

    // ── Informations parent / tuteur ───────────────────────────────
    @Column(name = "nom_parent", length = 150)
    private String nomParent;

    @Column(name = "telephone_parent", length = 20)
    private String telephoneParent;

    @Column(name = "email_parent", length = 120)
    private String emailParent;

    @Column(name = "adresse_parent", length = 255)
    private String adresseParent;

    // Statut géré par M07 (cursus) — INSCRIT par défaut
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_eleve")
    @Builder.Default
    private StatutEleve statutEleve = StatutEleve.INSCRIT;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_enseignement")
    private NiveauEnseignement niveauEnseignement;

    @Column(name = "date_inscription")
    private LocalDate dateInscription;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // ── Clé étrangère → ClasseScolaire ────────────────────────────
    // Nullable : un élève peut être inscrit sans classe encore affectée
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name       = "classe_id",
            foreignKey = @ForeignKey(name = "fk_eleve_classe")
    )
    private ClasseScolaire classeScolaire;

    // ── Clé étrangère → Etablissement ─────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name       = "etablissement_id",
            nullable   = false,
            foreignKey = @ForeignKey(name = "fk_eleve_etablissement")
    )
    private Etablissement etablissement;

    // ── Clé étrangère → AnneeScolaire ─────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name       = "annee_scolaire_id",
            foreignKey = @ForeignKey(name = "fk_eleve_annee_scolaire")
    )
    private AnneeScolaire anneeScolaire;

    // ── Clé étrangère → Utilisateur (compte connexion optionnel) ──
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name       = "utilisateur_id",
            foreignKey = @ForeignKey(name = "fk_eleve_utilisateur")
    )
    private Utilisateur utilisateur;

    @PrePersist
    protected void onCreate() {
        this.dateCreation     = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        if (this.dateInscription == null) {
            this.dateInscription = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}