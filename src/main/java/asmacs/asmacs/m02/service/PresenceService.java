package asmacs.asmacs.m02.service;

import asmacs.asmacs.m02.dto.PresenceDTO;
import asmacs.asmacs.m02.dto.SeanceDTO;
import asmacs.asmacs.m02.dto.StatPresenceDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PresenceService {

    // ── SÉANCES ────────────────────────────────────────────
    SeanceDTO creerSeance(SeanceDTO dto);
    SeanceDTO getSeanceById(Long id);
    List<SeanceDTO> getSeancesEnCours();
    List<SeanceDTO> getSeancesByDate(LocalDate date);
    List<SeanceDTO> getSeancesByClasse(Long classeId);
    List<SeanceDTO> getSeancesByEnseignant(Long enseignantId);
    SeanceDTO demarrerSeance(Long seanceId);
    SeanceDTO terminerSeance(Long seanceId);
    String genererQRCode(Long seanceId);

    // ── PRÉSENCES ──────────────────────────────────────────
    PresenceDTO marquerPresence(Long seanceId, Long eleveId,
                                String methode,
                                Double latitude, Double longitude);
    PresenceDTO marquerParQRCode(String token, Long eleveId,
                                 Double latitude, Double longitude);
    PresenceDTO modifierPresence(Long presenceId, String statut,
                                 String motif);
    PresenceDTO justifierAbsence(Long presenceId, String motif);
    List<PresenceDTO> getPresencesBySeance(Long seanceId);
    List<PresenceDTO> getPresencesByEleve(Long eleveId);
    List<PresenceDTO> getFraudes();

    // ── STATISTIQUES ───────────────────────────────────────
    StatPresenceDTO getStatsByClasse(Long classeId);
    StatPresenceDTO getStatsByEleve(Long eleveId);
    List<Map<String, Object>> getAbsentsFrequents();
    long countPresentsAujourdhui();
    long countAbsentsAujourdhui();
    long countFraudes();
    long countSeancesEnCours();
}