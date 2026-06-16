package asmacs.asmacs.m03.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Bulletin complet d'un élève pour une séquence. */
@Data
@Builder
public class BulletinVueDTO {
    private Long eleveId;
    private String eleveNom;
    private String classeNom;
    private Integer sequence;
    private List<BulletinLigneDTO> lignes;
    private String moyGenerale;
    private Integer rang;
    private Integer effectif;
}
