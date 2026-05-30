package asmacs.asmacs.m02.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatPresenceDTO {
    private String label;
    private long   nbPresents;
    private long   nbAbsents;
    private long   nbRetards;
    private long   nbTotal;
    private long   nbFraudes;

    public double getTauxPresence() {
        if (nbTotal == 0) return 0;
        return Math.round(((double) nbPresents / nbTotal) * 100.0);
    }
}