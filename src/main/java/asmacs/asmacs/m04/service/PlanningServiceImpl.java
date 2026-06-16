package asmacs.asmacs.m04.service;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.m04.dto.CelluleDTO;
import asmacs.asmacs.m04.dto.EmploiTempsVueDTO;
import asmacs.asmacs.m04.dto.LigneEdtDTO;
import asmacs.asmacs.m04.entity.SeanceCours;
import asmacs.asmacs.m04.repository.SeanceCoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanningServiceImpl implements PlanningService {

    private static final List<String> JOURS = List.of("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi");

    private final SeanceCoursRepository seanceRepo;
    private final ClasseScolaireRepository classeRepo;

    @Override
    public EmploiTempsVueDTO emploiClasse(Long classeId) {
        ClasseScolaire classe = classeRepo.findById(classeId).orElseThrow();
        List<SeanceCours> seances = seanceRepo.findByClasseScolaire_IdOrderByHeureDebutAscJourAsc(classeId);

        // Créneaux distincts triés
        TreeMap<LocalTime, LocalTime> creneaux = new TreeMap<>();
        for (SeanceCours s : seances) creneaux.put(s.getHeureDebut(), s.getHeureFin());

        // Index séance par (heureDebut, jour)
        Map<String, SeanceCours> idx = new HashMap<>();
        for (SeanceCours s : seances) idx.put(s.getHeureDebut() + "#" + s.getJour(), s);

        List<LigneEdtDTO> lignes = new ArrayList<>();
        for (Map.Entry<LocalTime, LocalTime> cr : creneaux.entrySet()) {
            List<CelluleDTO> cellules = new ArrayList<>();
            for (int jour = 1; jour <= 5; jour++) {
                SeanceCours s = idx.get(cr.getKey() + "#" + jour);
                cellules.add(s == null ? vide() : cellule(s));
            }
            lignes.add(LigneEdtDTO.builder()
                    .creneau(label(cr.getKey(), cr.getValue()))
                    .cellules(cellules)
                    .build());
        }

        return EmploiTempsVueDTO.builder()
                .classeId(classeId)
                .classeNom(classe.getNom())
                .jours(JOURS)
                .lignes(lignes)
                .nbConflits(conflits())
                .nbSeances(seances.size())
                .build();
    }

    // ─────────────────────────────────────── helpers
    private static CelluleDTO vide() {
        return CelluleDTO.builder().vide(true).bg("#F4F1E9").fg("#9BA89F").build();
    }

    private static CelluleDTO cellule(SeanceCours s) {
        String[] col = couleur(s.getMatiere().getCode());
        Enseignant e = s.getEnseignant();
        return CelluleDTO.builder()
                .vide(false)
                .matiereNom(s.getMatiere().getNom())
                .prof(e == null ? "" : "M. " + e.getNom())
                .salle(s.getSalle())
                .bg(col[0]).fg(col[1])
                .build();
    }

    /** Palette par code matière (cohérente avec le design). */
    private static String[] couleur(String code) {
        return switch (code == null ? "" : code) {
            case "MATH" -> new String[]{"#E7F1EB", "#0B5C3B"};
            case "PHYS" -> new String[]{"#FBF1D5", "#8A6504"};
            case "EN"   -> new String[]{"#FAE5E9", "#9A0C22"};
            case "FR"   -> new String[]{"#FAE5E9", "#9A0C22"};
            case "SVT"  -> new String[]{"#E6F0F4", "#1E5A78"};
            case "HG"   -> new String[]{"#EDEAF7", "#3F329A"};
            case "EPS"  -> new String[]{"#E6F0F4", "#1E5A78"};
            default      -> new String[]{"#E7F1EB", "#0B5C3B"};
        };
    }

    private static String label(LocalTime d, LocalTime f) {
        return String.format("%02d–%02d", d.getHour(), f.getHour());
    }

    /** Conflit = un même enseignant placé sur deux classes au même créneau/jour. */
    private int conflits() {
        Map<String, Integer> compteur = new HashMap<>();
        for (SeanceCours s : seanceRepo.findAll()) {
            if (s.getEnseignant() == null) continue;
            String k = s.getEnseignant().getId() + "#" + s.getJour() + "#" + s.getHeureDebut();
            compteur.merge(k, 1, Integer::sum);
        }
        return (int) compteur.values().stream().filter(n -> n > 1).count();
    }
}
