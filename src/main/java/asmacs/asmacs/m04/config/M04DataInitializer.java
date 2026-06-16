package asmacs.asmacs.m04.config;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.entity.Matiere;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.core.repository.EnseignantRepository;
import asmacs.asmacs.core.repository.MatiereRepository;
import asmacs.asmacs.m04.entity.SeanceCours;
import asmacs.asmacs.m04.repository.SeanceCoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Données de démonstration M04 : emploi du temps hebdomadaire de la 3ᵉ A
 * (généré sans conflit). Idempotent.
 */
@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class M04DataInitializer implements CommandLineRunner {

    private final ClasseScolaireRepository classeRepo;
    private final MatiereRepository matiereRepo;
    private final EnseignantRepository enseignantRepo;
    private final SeanceCoursRepository seanceRepo;

    // Créneaux : {heureDebut, heureFin}
    private static final LocalTime[][] CRENEAUX = {
            {LocalTime.of(7, 0), LocalTime.of(8, 0)},
            {LocalTime.of(8, 0), LocalTime.of(10, 0)},
            {LocalTime.of(10, 0), LocalTime.of(12, 0)},
            {LocalTime.of(14, 0), LocalTime.of(16, 0)},
    };

    // Grille [créneau][jour 0..4] = code matière (null = libre)
    private static final String[][] GRILLE = {
            {"MATH", "PHYS", "EN",   "MATH", "SVT"},
            {"HG",   "MATH", "SVT",  "FR",   "PHYS"},
            {"EN",   "HG",   "MATH", "PHYS", null},
            {"EPS",  "FR",   null,   "HG",   "MATH"},
    };

    @Override
    @Transactional
    public void run(String... args) {
        ClasseScolaire c3a = classeRepo.findAll().stream()
                .filter(c -> "3A".equals(c.getCode())).findFirst().orElse(null);
        if (c3a == null) { log.warn("M04 : classe 3A introuvable, EDT démo ignoré"); return; }
        if (seanceRepo.existsByClasseScolaire_Id(c3a.getId())) {
            log.info("  ✔ M04 : EDT déjà présent, semis ignoré");
            return;
        }

        Map<String, Matiere> mats = new HashMap<>();
        for (Matiere m : matiereRepo.findAll()) mats.put(m.getCode(), m);
        Enseignant ens = enseignantRepo.findAll().stream().findFirst().orElse(null);

        int n = 0;
        for (int slot = 0; slot < CRENEAUX.length; slot++) {
            for (int jour = 0; jour < 5; jour++) {
                String code = GRILLE[slot][jour];
                if (code == null) continue;
                Matiere matiere = mats.get(code);
                if (matiere == null) continue;
                seanceRepo.save(SeanceCours.builder()
                        .jour(jour + 1)
                        .heureDebut(CRENEAUX[slot][0])
                        .heureFin(CRENEAUX[slot][1])
                        .salle("Salle " + (12 + jour))
                        .classeScolaire(c3a)
                        .matiere(matiere)
                        .enseignant(ens)
                        .build());
                n++;
            }
        }
        log.info("  ✔ M04 : {} séances d'emploi du temps créées (3ᵉ A)", n);
    }
}
