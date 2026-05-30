package asmacs.asmacs.m01.entity;

import asmacs.asmacs.core.entity.AnneeScolaire;
import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Etablissement;
import asmacs.asmacs.core.entity.Utilisateur;
import asmacs.asmacs.core.enums.NiveauEnseignement;
import asmacs.asmacs.core.enums.Sexe;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidature")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_candidature", unique = true, length = 30)
    private String numeroCandidature;

    // ── Infos personnelles ──────────────────────────────────
    @Column(name = "nom", nullable = false, length = 80)
    private String nom;

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

    @Column(name = "adresse", length = 255)
    private String adresse;

    // ── Infos parent ────────────────────────────────────────
    @Column(name = "nom_parent", length = 150)
    private String nomParent;

    @Column(name = "telephone_parent", length = 20)
    private String telephoneParent;

    @Column(name = "email_parent", length = 120)
    private String emailParent;

    // ── Infos académiques ───────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_demande")
    private NiveauEnseignement niveauDemande;

    @Column(name = "filiere_demandee", length = 50)
    private String filiereDemandee;

    @Column(name = "etablissement_precedent", length = 150)
    private String etablissementPrecedent;

    @Column(name = "derniere_classe", length = 50)
    private String derniereClasse;

    @Column(name = "moyenne_precedente")
    private Double moyennePrecedente;

    // ── Statut candidature ──────────────────────────────────
    // EN_ATTENTE | DOCUMENTS_REQUIS | PAIEMENT_ATTENTE
    // TEST_ATTENTE | VALIDE | REJETE
    @Column(name = "statut", length = 30)
    @Builder.Default
    private String statut = "EN_ATTENTE";

    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    @Column(name = "observations", length = 500)
    private String observations;

    // ── Test d'entrée ───────────────────────────────────────
    @Column(name = "score_test")
    private Integer scoreTest;

    @Column(name = "test_effectue")
    @Builder.Default
    private Boolean testEffectue = false;

    @Column(name = "date_test")
    private LocalDateTime dateTest;

    // ── Après validation ────────────────────────────────────
    @Column(name = "matricule_genere", length = 30)
    private String matriculeGenere;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "date_rejet")
    private LocalDateTime dateRejet;

    // ── Timestamps ──────────────────────────────────────────
    @Column(name = "date_soumission", updatable = false)
    private LocalDateTime dateSoumission;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // ── FK → Etablissement ──────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "etablissement_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_candidature_etablissement")
    )
    private Etablissement etablissement;

    // ── FK → AnneeScolaire ──────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "annee_scolaire_id",
            foreignKey = @ForeignKey(name = "fk_candidature_annee")
    )
    private AnneeScolaire anneeScolaire;

    // ── FK → Classe affectée après validation ───────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "classe_affectee_id",
            foreignKey = @ForeignKey(name = "fk_candidature_classe")
    )
    private ClasseScolaire classeAffectee;

    // ── FK → Compte créé après validation ───────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "utilisateur_cree_id",
            foreignKey = @ForeignKey(name = "fk_candidature_utilisateur")
    )
    private Utilisateur utilisateurCree;

    // ── Documents uploadés ──────────────────────────────────
    @OneToMany(
            mappedBy = "candidature",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<DocumentCandidat> documents = new ArrayList<>();

    // ── Paiement ────────────────────────────────────────────
    @OneToOne(
            mappedBy = "candidature",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private PaiementAdmission paiement;

    @PrePersist
    protected void onCreate() {
        this.dateSoumission  = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}