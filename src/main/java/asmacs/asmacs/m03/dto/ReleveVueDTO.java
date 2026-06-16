package asmacs.asmacs.m03.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Relevé complet d'une classe pour une matière + statistiques de classe. */
@Data
@Builder
public class ReleveVueDTO {
    private Long classeId;
    private Long matiereId;
    private String classeNom;
    private String matiereNom;
    private Integer sequence;
    private List<ReleveLigneDTO> lignes;
    private String moyClasse;       // formatée
    private Integer tauxReussite;   // % d'élèves >= 10
    private int[] distribution;     // 4 tranches : 0-8, 8-10, 10-14, 14-20 (en %)
    private Integer effectif;
}
