package asmacs.asmacs.core.entity;

import asmacs.asmacs.core.enums.NiveauEnseignement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matiere")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matiere {

    // Clé primaire — référencée dans enseignant_matiere et classe_matiere
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Le code est obligatoire")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code; // ex: MATH, FR, SVT

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    // Poids dans le calcul de moyenne — utilisé par M03
    @Column(name = "coefficient", nullable = false)
    @Builder.Default
    private Integer coefficient = 1;

    @Column(name = "volume_horaire_hebdo")
    private Integer volumeHoraireHebdo;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_enseignement")
    private NiveauEnseignement niveauEnseignement;

    // ex: A4, C, D, Sciences, Lettres — null = toutes filières
    @Column(name = "filiere", length = 50)
    private String filiere;

    @Column(name = "obligatoire")
    @Builder.Default
    private Boolean obligatoire = true;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    // ── Relation inverse ManyToMany ────────────────────────────────
    // La table pivot est définie côté Enseignant
    @ManyToMany(mappedBy = "matieres", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Enseignant> enseignants = new ArrayList<>();

    // La table pivot est définie côté ClasseScolaire
    @ManyToMany(mappedBy = "matieres", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ClasseScolaire> classes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}