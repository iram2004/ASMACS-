package asmacs.asmacs.m03.service;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Eleve;
import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.entity.Matiere;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.core.repository.EleveRepository;
import asmacs.asmacs.core.repository.EnseignantRepository;
import asmacs.asmacs.core.repository.MatiereRepository;
import asmacs.asmacs.m03.dto.*;
import asmacs.asmacs.m03.entity.Evaluation;
import asmacs.asmacs.m03.entity.Note;
import asmacs.asmacs.m03.repository.EvaluationRepository;
import asmacs.asmacs.m03.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PedagogieServiceImpl implements PedagogieService {

    static final String[] TYPES = {"INTERRO", "DEVOIR", "COMPO"};

    private final EvaluationRepository evalRepo;
    private final NoteRepository noteRepo;
    private final EleveRepository eleveRepo;
    private final ClasseScolaireRepository classeRepo;
    private final MatiereRepository matiereRepo;
    private final EnseignantRepository enseignantRepo;

    // ─────────────────────────────────────────────── RELEVÉ
    @Override
    @Transactional(readOnly = true)
    public ReleveVueDTO releve(Long classeId, Long matiereId, Integer sequence) {
        ClasseScolaire classe = classeRepo.findById(classeId).orElseThrow();
        Matiere matiere = matiereRepo.findById(matiereId).orElseThrow();
        List<Eleve> eleves = eleveRepo.findByClasseScolaireId(classeId);

        Map<String, Evaluation> byType = evalRepo
                .findByClasseScolaire_IdAndMatiere_IdAndSequence(classeId, matiereId, sequence)
                .stream().collect(Collectors.toMap(Evaluation::getType, e -> e, (a, b) -> a));

        // Calcul par élève
        List<double[]> tmp = new ArrayList<>();   // [interro, devoir, compo, moy] (NaN = absent)
        List<Eleve> ordered = new ArrayList<>(eleves);
        Map<Long, Double> moyByEleve = new HashMap<>();
        for (Eleve el : eleves) {
            double itr = noteVal(el.getId(), byType.get("INTERRO"));
            double dev = noteVal(el.getId(), byType.get("DEVOIR"));
            double cmp = noteVal(el.getId(), byType.get("COMPO"));
            Double moy = moyenne(itr, dev, cmp);
            moyByEleve.put(el.getId(), moy);
            tmp.add(new double[]{itr, dev, cmp, moy == null ? Double.NaN : moy});
        }

        // Tri par moyenne décroissante (sans note = en bas) pour le rang
        ordered.sort((a, b) -> Double.compare(
                nz(moyByEleve.get(b.getId())), nz(moyByEleve.get(a.getId()))));

        List<ReleveLigneDTO> lignes = new ArrayList<>();
        int rang = 0;
        for (Eleve el : ordered) {
            Double moy = moyByEleve.get(el.getId());
            rang++;
            lignes.add(ReleveLigneDTO.builder()
                    .eleveId(el.getId())
                    .nom(el.getNom() + " " + el.getPrenom())
                    .ini(ini(el))
                    .interro(fmt(noteValN(el.getId(), byType.get("INTERRO"))))
                    .devoir(fmt(noteValN(el.getId(), byType.get("DEVOIR"))))
                    .compo(fmt(noteValN(el.getId(), byType.get("COMPO"))))
                    .moy(fmt(moy))
                    .rang(moy == null ? null : rang)
                    .appreciation(appreciation(moy))
                    .moyStyle(moyStyle(moy))
                    .build());
        }

        // Statistiques
        List<Double> moys = moyByEleve.values().stream().filter(Objects::nonNull).toList();
        Double moyClasse = moys.isEmpty() ? null : moys.stream().mapToDouble(d -> d).average().orElse(0);
        int reussite = moys.isEmpty() ? 0
                : (int) Math.round(100.0 * moys.stream().filter(m -> m >= 10).count() / moys.size());
        int[] dist = distribution(moys);

        return ReleveVueDTO.builder()
                .classeId(classeId).matiereId(matiereId)
                .classeNom(classe.getNom()).matiereNom(matiere.getNom())
                .sequence(sequence)
                .lignes(lignes)
                .moyClasse(fmt(moyClasse))
                .tauxReussite(reussite)
                .distribution(dist)
                .effectif(eleves.size())
                .build();
    }

    // ─────────────────────────────────────────────── BULLETIN
    @Override
    @Transactional(readOnly = true)
    public BulletinVueDTO bulletin(Long eleveId, Integer sequence) {
        Eleve eleve = eleveRepo.findById(eleveId).orElseThrow();
        ClasseScolaire classe = eleve.getClasseScolaire();
        List<Matiere> matieres = new ArrayList<>(classe.getMatieres());
        matieres.sort(Comparator.comparing(Matiere::getNom));

        List<BulletinLigneDTO> lignes = new ArrayList<>();
        for (Matiere m : matieres) {
            List<Evaluation> evals = evalRepo
                    .findByClasseScolaire_IdAndMatiere_IdAndSequence(classe.getId(), m.getId(), sequence);
            Double moy = moyenneMatiere(eleveId, evals);
            lignes.add(BulletinLigneDTO.builder()
                    .matiereNom(m.getNom())
                    .moy(fmt(moy))
                    .coef(m.getCoefficient())
                    .prof(profOf(evals))
                    .appreciation(appreciation(moy))
                    .moyStyle(moyStyle(moy))
                    .build());
        }

        Double generale = generale(eleveId, matieres, classe.getId(), sequence);

        // Rang : position parmi les élèves de la classe (moyenne générale décroissante)
        List<Eleve> eleves = eleveRepo.findByClasseScolaireId(classe.getId());
        List<Double> gens = eleves.stream()
                .map(e -> generale(e.getId(), matieres, classe.getId(), sequence))
                .filter(Objects::nonNull).sorted(Comparator.reverseOrder()).toList();
        Integer rang = (generale == null) ? null
                : (int) (gens.stream().filter(g -> g > generale).count() + 1);

        return BulletinVueDTO.builder()
                .eleveId(eleveId)
                .eleveNom(eleve.getNom() + " " + eleve.getPrenom())
                .classeNom(classe.getNom())
                .sequence(sequence)
                .lignes(lignes)
                .moyGenerale(fmt(generale))
                .rang(rang)
                .effectif(eleves.size())
                .build();
    }

    // ─────────────────────────────────────────────── SAISIE
    @Override
    public void saveSaisie(Long classeId, Long matiereId, Integer sequence, Long enseignantId, Map<String, String> notes) {
        ClasseScolaire classe = classeRepo.findById(classeId).orElseThrow();
        Matiere matiere = matiereRepo.findById(matiereId).orElseThrow();
        Enseignant ens = enseignantId == null ? null : enseignantRepo.findById(enseignantId).orElse(null);

        Map<String, Evaluation> byType = new HashMap<>();
        for (Map.Entry<String, String> e : notes.entrySet()) {
            if (!e.getKey().startsWith("note_")) continue;
            String[] p = e.getKey().split("_"); // note, eleveId, TYPE
            if (p.length != 3) continue;
            String val = e.getValue() == null ? "" : e.getValue().trim();
            if (val.isEmpty()) continue;
            Double valeur;
            try {
                valeur = Double.parseDouble(val.replace(',', '.'));
            } catch (NumberFormatException ex) {
                continue;
            }
            Long eleveId = Long.valueOf(p[1]);
            String type = p[2];
            if (!Arrays.asList(TYPES).contains(type)) continue;

            Evaluation eval = byType.computeIfAbsent(type, t ->
                    ensureEvaluation(classe, matiere, sequence, t, ens));
            Eleve el = eleveRepo.findById(eleveId).orElse(null);
            if (el == null) continue;
            Note note = noteRepo.findByEleve_IdAndEvaluation_Id(eleveId, eval.getId())
                    .orElseGet(() -> Note.builder().eleve(el).evaluation(eval).build());
            note.setValeur(valeur);
            noteRepo.save(note);
        }
    }

    private Evaluation ensureEvaluation(ClasseScolaire classe, Matiere matiere, Integer sequence, String type, Enseignant ens) {
        return evalRepo.findByClasseScolaire_IdAndMatiere_IdAndSequenceAndType(
                        classe.getId(), matiere.getId(), sequence, type)
                .orElseGet(() -> evalRepo.save(Evaluation.builder()
                        .type(type).sequence(sequence).bareme(20.0)
                        .libelle(libelle(type) + " · Séquence " + sequence)
                        .dateEvaluation(LocalDate.now())
                        .classeScolaire(classe).matiere(matiere).enseignant(ens)
                        .build()));
    }

    // ─────────────────────────────────────────────── helpers calcul
    private Double moyenneMatiere(Long eleveId, List<Evaluation> evals) {
        double sum = 0;
        int n = 0;
        for (Evaluation e : evals) {
            double v = noteVal(eleveId, e);
            if (!Double.isNaN(v)) { sum += v; n++; }
        }
        return n == 0 ? null : sum / n;
    }

    /** Moyenne pondérée générale d'un élève sur ses matières (coef matière). */
    private Double generale(Long eleveId, List<Matiere> matieres, Long classeId, Integer sequence) {
        double sumW = 0, sumC = 0;
        for (Matiere m : matieres) {
            List<Evaluation> evals = evalRepo
                    .findByClasseScolaire_IdAndMatiere_IdAndSequence(classeId, m.getId(), sequence);
            Double moy = moyenneMatiere(eleveId, evals);
            if (moy != null) { sumW += moy * m.getCoefficient(); sumC += m.getCoefficient(); }
        }
        return sumC == 0 ? null : sumW / sumC;
    }

    private double noteVal(Long eleveId, Evaluation eval) {
        if (eval == null) return Double.NaN;
        return noteRepo.findByEleve_IdAndEvaluation_Id(eleveId, eval.getId())
                .map(Note::getValeur)
                .map(v -> v == null ? Double.NaN
                        : (eval.getBareme() != null && eval.getBareme() > 0 ? v / eval.getBareme() * 20.0 : v))
                .orElse(Double.NaN);
    }

    /** Valeur brute (sur barème) pour affichage colonne, ou null. */
    private Double noteValN(Long eleveId, Evaluation eval) {
        if (eval == null) return null;
        return noteRepo.findByEleve_IdAndEvaluation_Id(eleveId, eval.getId())
                .map(Note::getValeur).orElse(null);
    }

    private static Double moyenne(double itr, double dev, double cmp) {
        double sum = 0; int n = 0;
        if (!Double.isNaN(itr)) { sum += itr; n++; }
        if (!Double.isNaN(dev)) { sum += dev; n++; }
        if (!Double.isNaN(cmp)) { sum += cmp; n++; }
        return n == 0 ? null : sum / n;
    }

    private static int[] distribution(List<Double> moys) {
        if (moys.isEmpty()) return new int[]{0, 0, 0, 0};
        int[] c = new int[4];
        for (double m : moys) {
            if (m < 8) c[0]++;
            else if (m < 10) c[1]++;
            else if (m < 14) c[2]++;
            else c[3]++;
        }
        int[] pct = new int[4];
        for (int i = 0; i < 4; i++) pct[i] = (int) Math.round(100.0 * c[i] / moys.size());
        return pct;
    }

    private static double nz(Double d) { return d == null ? -1 : d; }

    private static String ini(Eleve e) {
        String a = e.getNom() == null || e.getNom().isEmpty() ? "" : e.getNom().substring(0, 1);
        String b = e.getPrenom() == null || e.getPrenom().isEmpty() ? "" : e.getPrenom().substring(0, 1);
        return (a + b).toUpperCase();
    }

    private static String profOf(List<Evaluation> evals) {
        for (Evaluation e : evals) {
            if (e.getEnseignant() != null) {
                Enseignant p = e.getEnseignant();
                return "M. " + p.getNom();
            }
        }
        return "—";
    }

    private static String libelle(String type) {
        return switch (type) {
            case "INTERRO" -> "Interrogation";
            case "DEVOIR" -> "Devoir";
            case "COMPO" -> "Composition";
            default -> type;
        };
    }

    private static String fmt(Double v) {
        return v == null ? "—" : String.format(Locale.FRANCE, "%.1f", v);
    }

    private static String appreciation(Double moy) {
        if (moy == null) return "—";
        if (moy >= 16) return "Excellent";
        if (moy >= 14) return "Très bien";
        if (moy >= 12) return "Bien";
        if (moy >= 10) return "Assez bien";
        if (moy >= 8) return "Insuffisant";
        return "Doit progresser";
    }

    private static String moyStyle(Double moy) {
        String c = moy == null ? "#9BA89F" : (moy >= 14 ? "#0F7B4F" : (moy >= 10 ? "#B5870A" : "#C8102E"));
        return "color:" + c + ";font-weight:800;";
    }
}
