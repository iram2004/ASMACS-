package asmacs.asmacs.m04.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Une ligne de la grille = un créneau horaire et ses 5 cases (Lun→Ven). */
@Data
@Builder
public class LigneEdtDTO {
    private String creneau;          // ex. "07–08"
    private List<CelluleDTO> cellules; // taille 5
}
