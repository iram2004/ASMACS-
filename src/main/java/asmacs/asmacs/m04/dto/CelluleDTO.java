package asmacs.asmacs.m04.dto;

import lombok.Builder;
import lombok.Data;

/** Une case de la grille d'emploi du temps (ou case vide = « Libre »). */
@Data
@Builder
public class CelluleDTO {
    private boolean vide;
    private String matiereNom;
    private String prof;
    private String salle;
    private String bg;   // couleur de fond
    private String fg;   // couleur de texte
}
