package asmacs.asmacs.core.entity;

import asmacs.asmacs.core.enums.NiveauEnseignement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etablissement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etablissement {

    // Clé primaire auto-incrémentée — référencée en FK par
    // AnneeScolaire, ClasseScolaire, Eleve, Enseignant, Utilisateur
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "code_etablissement", unique = true, length = 20)
    private String codeEtablissement;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "directeur", length = 100)
    private String directeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_enseignement", nullable = false)
    private NiveauEnseignement niveauEnseignement;

    // PUBLIC ou PRIVE
    @Column(name = "public_prive", length = 10)
    private String publicPrive;

    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // ── Relations OneToMany ────────────────────────────────────────
    // Cascades : si on supprime un établissement, tout est supprimé

    @OneToMany(
            mappedBy      = "etablissement",
            cascade       = CascadeType.ALL,
            fetch         = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<AnneeScolaire> anneesScolaires = new ArrayList<>();

    @OneToMany(
            mappedBy      = "etablissement",
            cascade       = CascadeType.ALL,
            fetch         = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ClasseScolaire> classes = new ArrayList<>();

    @OneToMany(
            mappedBy = "etablissement",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<Utilisateur> utilisateurs = new ArrayList<>();

    @OneToMany(
            mappedBy = "etablissement",
            cascade  = CascadeType.ALL,
            fetch    = FetchType.LAZY
    )
    @Builder.Default
    private List<Enseignant> enseignants = new ArrayList<>();

    @OneToMany(
            mappedBy      = "etablissement",
            cascade       = CascadeType.ALL,
            fetch         = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Eleve> eleves = new ArrayList<>();

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