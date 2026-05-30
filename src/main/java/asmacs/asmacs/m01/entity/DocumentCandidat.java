package asmacs.asmacs.m01.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_candidat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCandidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TYPE : ACTE_NAISSANCE | BULLETIN | PHOTO | DIPLOME | CNI_PARENT
    @Column(name = "type_document", nullable = false, length = 30)
    private String typeDocument;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    // Chemin de stockage sur le serveur
    @Column(name = "chemin_fichier", nullable = false, length = 500)
    private String cheminFichier;

    @Column(name = "taille_fichier")
    private Long tailleFichier;

    @Column(name = "type_mime", length = 50)
    private String typeMime;

    @Column(name = "date_upload", updatable = false)
    private LocalDateTime dateUpload;

    // ── FK → Candidature ────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidature_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_document_candidature")
    )
    private Candidature candidature;

    @PrePersist
    protected void onCreate() {
        this.dateUpload = LocalDateTime.now();
    }
}