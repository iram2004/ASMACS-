package asmacs.asmacs.m01.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement_admission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementAdmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MTN_MOMO | ORANGE_MONEY
    @Column(name = "operateur", length = 20)
    private String operateur;

    @Column(name = "numero_telephone", length = 20)
    private String numeroTelephone;

    @Column(name = "reference_transaction", length = 100)
    private String referenceTransaction;

    @Column(name = "montant")
    private Double montant;

    // EN_ATTENTE | EFFECTUE | ECHEC | REMBOURSE
    @Column(name = "statut", length = 20)
    @Builder.Default
    private String statut = "EN_ATTENTE";

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    // ── FK → Candidature (OneToOne) ─────────────────────────
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidature_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_paiement_candidature")
    )
    private Candidature candidature;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}