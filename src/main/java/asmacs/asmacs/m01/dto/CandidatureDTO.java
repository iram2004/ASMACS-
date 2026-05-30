package asmacs.asmacs.m01.dto;

import asmacs.asmacs.core.enums.NiveauEnseignement;
import asmacs.asmacs.core.enums.Sexe;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureDTO {

    private Long   id;
    private String numeroCandidature;
    private String statut;
    private String motifRejet;
    private String observations;

    // Infos personnelles
    private String    nom;
    private String    prenom;
    private Sexe      sexe;
    private LocalDate dateNaissance;
    private String    lieuNaissance;
    private String    telephone;
    private String    email;
    private String    adresse;

    // Infos parent
    private String nomParent;
    private String telephoneParent;
    private String emailParent;

    // Infos académiques
    private NiveauEnseignement niveauDemande;
    private String filiereDemandee;
    private String etablissementPrecedent;
    private String derniereClasse;
    private Double moyennePrecedente;

    // Test
    private Integer  scoreTest;
    private Boolean  testEffectue;
    private LocalDateTime dateTest;

    // Après validation
    private String        matriculeGenere;
    private String        classeAffecteeNom;
    private LocalDateTime dateValidation;

    // Paiement
    private String  paiementStatut;
    private String  paiementOperateur;
    private String  paiementReference;
    private Double  paiementMontant;

    // Documents
    private List<String> typesDocumentsPresents;

    // Contexte
    private String        etablissementNom;
    private String        anneeScolaireLibelle;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;

    // Utilitaires Thymeleaf
    public String getNomComplet() {
        return nom + " " + prenom;
    }

    public boolean isEnAttente() {
        return "EN_ATTENTE".equals(statut);
    }

    public boolean isValide() {
        return "VALIDE".equals(statut);
    }

    public boolean isRejete() {
        return "REJETE".equals(statut);
    }

    public boolean isPaiementEffectue() {
        return "EFFECTUE".equals(paiementStatut);
    }
}