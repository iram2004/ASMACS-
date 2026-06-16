package asmacs.asmacs.m03.config;

import asmacs.asmacs.core.entity.*;
import asmacs.asmacs.core.enums.NiveauEnseignement;
import asmacs.asmacs.core.enums.Sexe;
import asmacs.asmacs.core.enums.StatutEleve;
import asmacs.asmacs.core.repository.*;
import asmacs.asmacs.m03.entity.Evaluation;
import asmacs.asmacs.m03.entity.Note;
import asmacs.asmacs.m03.repository.EvaluationRepository;
import asmacs.asmacs.m03.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Données de démonstration pour M03 : matières rattachées à la classe 3ᵉ A,
 * quelques élèves, évaluations (Interro/Devoir/Compo · Séquence 1) et notes.
 * S'exécute après {@code DataInitializer} et reste idempotent.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class M03DataInitializer implements CommandLineRunner {

    private final ClasseScolaireRepository classeRepo;
    private final MatiereRepository matiereRepo;
    private final EnseignantRepository enseignantRepo;
    private final EleveRepository eleveRepo;
    private final AnneeScolaireRepository anneeRepo;
    private final EvaluationRepository evalRepo;
    private final NoteRepository noteRepo;

    private static final String[] MATIERES_3A = {"MATH", "PHYS", "SVT", "FR", "EN", "HG", "EPS"};
    private static final String[] TYPES = {"INTERRO", "DEVOIR", "COMPO"};

    // nom | prenom | matricule | niveau moyen visé (base /20)
    private static final Object[][] ELEVES = {
            {"Nanda", "Arnold", "ELV-3A-001", 15},
            {"Keudjio", "Nely", "ELV-3A-002", 14},
            {"Domeni", "Merveille", "ELV-3A-003", 11},
            {"Kangue", "Dominique", "ELV-3A-004", 17},
            {"Kom", "William", "ELV-3A-005", 8},
            {"Epesse", "Yann", "ELV-3A-006", 9},
    };

    @Override
    @Transactional
    public void run(String... args) {
        ClasseScolaire c3a = classeRepo.findAll().stream()
                .filter(c -> "3A".equals(c.getCode())).findFirst().orElse(null);
        if (c3a == null) {
            log.warn("M03 : classe 3A introuvable, données démo ignorées");
            return;
        }

        // 1) Rattacher les matières à la classe 3A
        if (c3a.getMatieres() == null || c3a.getMatieres().isEmpty()) {
            List<Matiere> mats = new ArrayList<>();
            for (String code : MATIERES_3A) {
                matiereRepo.findAll().stream().filter(m -> code.equals(m.getCode()))
                        .findFirst().ifPresent(mats::add);
            }
            c3a.setMatieres(mats);
            classeRepo.save(c3a);
            log.info("  ✔ M03 : {} matières rattachées à 3ᵉ A", mats.size());
        }

        // 2) Créer les élèves de 3A
        List<Eleve> eleves3a = eleveRepo.findByClasseScolaireId(c3a.getId());
        if (eleves3a.isEmpty()) {
            Etablissement etab = c3a.getEtablissement();
            AnneeScolaire annee = c3a.getAnneeScolaire();
            for (Object[] d : ELEVES) {
                Eleve el = Eleve.builder()
                        .matricule((String) d[2])
                        .nom((String) d[0]).prenom((String) d[1])
                        .sexe(Sexe.MASCULIN)
                        .dateNaissance(LocalDate.of(2010, 3, 14))
                        .lieuNaissance("Douala").nationalite("Camerounaise")
                        .statutEleve(StatutEleve.ACTIF)
                        .niveauEnseignement(NiveauEnseignement.SECONDAIRE)
                        .dateInscription(LocalDate.now())
                        .classeScolaire(c3a).etablissement(etab).anneeScolaire(annee)
                        .build();
                eleveRepo.save(el);
            }
            eleves3a = eleveRepo.findByClasseScolaireId(c3a.getId());
            log.info("  ✔ M03 : {} élèves créés en 3ᵉ A", eleves3a.size());
        }

        // 3) Évaluations + notes (séquence 1)
        boolean dejaFait = !evalRepo.findByClasseScolaire_IdAndSequence(c3a.getId(), 1).isEmpty();
        if (dejaFait) {
            log.info("  ✔ M03 : évaluations déjà présentes, semis ignoré");
            return;
        }

        Enseignant ens = enseignantRepo.findAll().stream().findFirst().orElse(null);
        int notesCount = 0;
        for (String code : MATIERES_3A) {
            Matiere matiere = matiereRepo.findAll().stream()
                    .filter(m -> code.equals(m.getCode())).findFirst().orElse(null);
            if (matiere == null) continue;
            for (String type : TYPES) {
                Evaluation eval = evalRepo.save(Evaluation.builder()
                        .type(type).sequence(1).bareme(20.0)
                        .libelle(libelle(type) + " · Séquence 1")
                        .dateEvaluation(LocalDate.now())
                        .classeScolaire(c3a).matiere(matiere).enseignant(ens)
                        .build());
                int delta = switch (type) { case "INTERRO" -> -1; case "COMPO" -> 1; default -> 0; };
                for (Eleve el : eleves3a) {
                    int base = baseOf(el.getMatricule());
                    double v = clamp(base + delta + jitter(code));
                    noteRepo.save(Note.builder().eleve(el).evaluation(eval).valeur(v).build());
                    notesCount++;
                }
            }
        }
        log.info("  ✔ M03 : {} évaluations · {} notes générées (3ᵉ A · séquence 1)",
                MATIERES_3A.length * TYPES.length, notesCount);
    }

    private static int baseOf(String matricule) {
        for (Object[] d : ELEVES) if (d[2].equals(matricule)) return (int) d[3];
        return 12;
    }

    /** Petite variation déterministe par matière pour des moyennes réalistes. */
    private static double jitter(String code) {
        return ((code.hashCode() % 5) - 2) * 0.5; // -1.0 .. +1.0
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(20, Math.round(v * 2) / 2.0));
    }

    private static String libelle(String type) {
        return switch (type) {
            case "INTERRO" -> "Interrogation";
            case "DEVOIR" -> "Devoir";
            case "COMPO" -> "Composition";
            default -> type;
        };
    }
}
