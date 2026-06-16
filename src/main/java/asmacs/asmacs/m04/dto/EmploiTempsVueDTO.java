package asmacs.asmacs.m04.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Emploi du temps hebdomadaire d'une classe. */
@Data
@Builder
public class EmploiTempsVueDTO {
    private Long classeId;
    private String classeNom;
    private List<String> jours;        // libellés colonnes
    private List<LigneEdtDTO> lignes;  // une par créneau
    private int nbConflits;            // conflits enseignant détectés
    private int nbSeances;
}
