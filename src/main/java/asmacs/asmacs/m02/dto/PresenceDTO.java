package asmacs.asmacs.m02.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceDTO {
    private Long   id;
    private String statut;
    private String methodeValidation;
    private LocalDateTime heureArrivee;
    private String motifAbsence;
    private Boolean justifiee;
    private Boolean fraudeDetectee;
    private Double  scoreFacial;
    private String  noteObservation;

    // Élève
    private Long   eleveId;
    private String eleveNom;
    private String elevePrenom;
    private String eleveMatricule;

    // Séance
    private Long   seanceId;
    private String matiereNom;
    private String classeNom;
    private String enseignantNom;
    private String dateSeanceFormatee;
    private String heureDebutFormatee;

    public String getNomComplet() {
        return eleveNom + " " + elevePrenom;
    }

    public boolean isPresent()  { return "PRESENT".equals(statut); }
    public boolean isAbsent()   { return "ABSENT".equals(statut); }
    public boolean isRetard()   { return "RETARD".equals(statut); }
}