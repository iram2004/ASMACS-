package asmacs.asmacs.m04.entity;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.entity.Matiere;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * M04 — Une séance de l'emploi du temps (une « case » de la grille hebdomadaire) :
 * une classe a, un jour donné et un créneau horaire, une matière avec un enseignant
 * dans une salle.
 */
@Entity
@Table(
        name = "m04_seance_cours",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_edt_classe_jour_heure",
                columnNames = {"classe_id", "jour", "heure_debut"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeanceCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 1 = Lundi … 5 = Vendredi. */
    @Column(name = "jour", nullable = false)
    private Integer jour;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "salle", length = 30)
    private String salle;

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
