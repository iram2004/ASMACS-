package asmacs.asmacs.m03.dto;

import lombok.Builder;
import lombok.Data;

/** Une ligne du relevé de notes d'une classe pour une matière (un élève). */
@Data
@Builder
public class ReleveLigneDTO {
    private Long eleveId;
    private String nom;
    private String ini;
    private String interro;   // notes formatées (ou "—")
    private String devoir;
    private String compo;
    private String moy;       // moyenne matière formatée
    private Integer rang;
    private String appreciation;
    private String moyStyle;  // style inline (couleur selon la moyenne)
}
