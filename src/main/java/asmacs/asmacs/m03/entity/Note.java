package asmacs.asmacs.m03.entity;

import asmacs.asmacs.core.entity.Eleve;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * M03 — Note d'un élève pour une évaluation donnée (sur le barème de l'évaluation).
 */
@Entity
@Table(
        name = "m03_note",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_note_eleve_evaluation",
                columnNames = {"eleve_id", "evaluation_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valeur")
    private Double valeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PreUpdate
    @PrePersist
    protected void touch() {
        this.dateModification = LocalDateTime.now();
    }
}
