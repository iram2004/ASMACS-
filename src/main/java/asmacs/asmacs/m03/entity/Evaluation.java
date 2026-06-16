package asmacs.asmacs.m03.entity;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.entity.Matiere;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * M03 — Une évaluation (interrogation, devoir ou composition) rattachée à une
 * classe, une matière, une séquence et un enseignant. Les notes des élèves y
 * sont rattachées via {@link Note}.
 */
@Entity
@Table(
        name = "m03_evaluation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_eval_classe_matiere_seq_type",
                columnNames = {"classe_id", "matiere_id", "sequence", "type"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INTERRO | DEVOIR | COMPO
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "libelle", length = 120)
    private String libelle;

    @Column(name = "date_evaluation")
    private LocalDate dateEvaluation;

    @Column(name = "bareme", nullable = false)
    @Builder.Default
    private Double bareme = 20.0;

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
