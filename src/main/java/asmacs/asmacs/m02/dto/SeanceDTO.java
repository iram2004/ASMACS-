package asmacs.asmacs.m02.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeanceDTO {
    private Long      id;
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String    typeSeance;
    private String    statut;
    private String    salle;
    private String    qrCodeToken;
    private Boolean   geofencingActif;
    private Double    latitudeAutorisee;
    private Double    longitudeAutorisee;
    private Integer   rayonAutorisationMetres;

    // Relations
    private Long   classeId;
    private String classeNom;
    private Long   matiereId;
    private String matiereNom;
    private Long   enseignantId;
    private String enseignantNom;
    private String anneeScolaireLibelle;

    // Stats
    private long nbPresents;
    private long nbAbsents;
    private long nbRetards;
    private long nbTotal;
    private long nbFraudes;

    public double getTauxPresence() {
        if (nbTotal == 0) return 0;
        return Math.round(((double) nbPresents / nbTotal) * 100.0);
    }
}