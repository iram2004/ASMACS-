package asmacs.asmacs.m03.entity;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.entity.Matiere;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * M03 — Entrée de cahier de texte : ce qui a été fait en classe à une date,
 * et les devoirs donnés. Alimente l'écran « Cahier de texte » du portail enseignant.
 */
@Entity
@Table(name = "m03_cahier_texte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CahierTexte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_seance", nullable = false)
    private LocalDate dateSeance;

    @Column(name = "titre", nullable = false, length = 160)
    private String titre;

    @Column(name = "contenu", length = 1000)
    private String contenu;

    @Column(name = "devoirs", length = 500)
    private String devoirs;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classe_id", nullable = false)
    private ClasseScolaire classeScolaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
