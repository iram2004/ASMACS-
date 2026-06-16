package asmacs.asmacs.m03.dto;

import lombok.Builder;
import lombok.Data;

/** Une ligne du bulletin d'un élève (une matière). */
@Data
@Builder
public class BulletinLigneDTO {
    private String matiereNom;
    private String moy;
    private Integer coef;
    private String prof;
    private String appreciation;
    private String moyStyle;
}
