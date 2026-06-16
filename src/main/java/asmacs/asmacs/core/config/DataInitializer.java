package asmacs.asmacs.core.config;

import asmacs.asmacs.core.entity.*;
import asmacs.asmacs.core.enums.*;
import asmacs.asmacs.core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EtablissementRepository  etablissementRepo;
    private final AnneeScolaireRepository  anneeScolaireRepo;
    private final UtilisateurRepository    utilisateurRepo;
    private final EnseignantRepository     enseignantRepo;
    private final EleveRepository          eleveRepo;
    private final ClasseScolaireRepository classeRepo;
    private final MatiereRepository        matiereRepo;
    private final PasswordEncoder          passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("╔══════════════════════════════════════╗");
        log.info("║  ASMACS — Initialisation données     ║");
        log.info("╚══════════════════════════════════════╝");

        Etablissement etab  = creerEtablissement();
        AnneeScolaire annee = creerAnneeScolaire(etab);
        creerUtilisateurs(etab);
        creerMatieres();
        creerClasses(etab, annee);
        creerEnseignants(etab);
        creerEleves(etab, annee);

        log.info("✅ Initialisation terminée avec succès");
    }

    // ─────────────────────────────────────────────────────────────
    private Etablissement creerEtablissement() {
        return etablissementRepo
                .findByCodeEtablissement("ASMACS-001")
                .orElseGet(() -> {
                    Etablissement e = Etablissement.builder()
                            .nom("Lycée Bilingue de Bonaberi")
                            .codeEtablissement("ASMACS-001")
                            .ville("Douala")
                            .region("Littoral")
                            .telephone("699000001")
                            .email("contact@lycee-bonaberi.cm")
                            .directeur("M. TCHAPTCHET Arnold")
                            .niveauEnseignement(NiveauEnseignement.SECONDAIRE)
                            .publicPrive("PUBLIC")
                            .build();
                    log.info("  ✔ Établissement : {}", e.getNom());
                    return etablissementRepo.save(e);
                });
    }

    private AnneeScolaire creerAnneeScolaire(Etablissement etab) {
        return anneeScolaireRepo
                .findByLibelleAndEtablissementId("2024-2025", etab.getId())
                .orElseGet(() -> {
                    AnneeScolaire a = AnneeScolaire.builder()
                            .libelle("2024-2025")
                            .dateDebut(LocalDate.of(2024, 9, 2))
                            .dateFin(LocalDate.of(2025, 6, 30))
                            .enCours(true)
                            .etablissement(etab)
                            .build();
                    log.info("  ✔ Année scolaire : {}", a.getLibelle());
                    return anneeScolaireRepo.save(a);
                });
    }

    private void creerUtilisateurs(Etablissement etab) {
        // username | mot de passe | nom | prenom | role
        Object[][] data = {
                {"admin",        "admin123",   "Admin",      "Système",    Role.ADMIN},
                {"directeur",    "dir123",     "Directeur",  "Principal",  Role.DIRECTEUR},
                {"enseignant1",  "ens123",     "Martin",     "Jean",       Role.ENSEIGNANT},
                {"eleve1",       "elv123",     "Dupont",     "Marie",      Role.ELEVE},
                {"parent1",      "par123",     "Dupont",     "Pierre",     Role.PARENT},
                {"comptable1",   "cpt123",     "Finance",    "Alice",      Role.COMPTABLE},
                {"dsi1",         "dsi123",     "Tech",       "Bob",        Role.DSI},
                {"secretaire1",  "sec123",     "Bureau",     "Claire",     Role.SECRETAIRE},
        };
        for (Object[] d : data) {
            String username = (String) d[0];
            if (!utilisateurRepo.existsByUsername(username)) {
                Utilisateur u = Utilisateur.builder()
                        .username(username)
                        .password(passwordEncoder.encode((String) d[1]))
                        .nom((String) d[2])
                        .prenom((String) d[3])
                        .email(username + "@asmacs.cm")
                        .role((Role) d[4])
                        .etablissement(etab)
                        .actif(true)
                        .build();
                utilisateurRepo.save(u);
                log.info("  ✔ Utilisateur : {} [{}]", username, d[4]);
            }
        }
    }

    private void creerMatieres() {
        // code | nom | coeff | niveau | filiere
        String[][] data = {
                {"MATH",  "Mathématiques",               "4", "SECONDAIRE", ""},
                {"PHYS",  "Physique-Chimie",             "3", "SECONDAIRE", ""},
                {"SVT",   "Sciences de la Vie",          "2", "SECONDAIRE", ""},
                {"FR",    "Français",                    "4", "SECONDAIRE", ""},
                {"EN",    "Anglais",                     "3", "SECONDAIRE", ""},
                {"HG",    "Histoire-Géographie",         "2", "SECONDAIRE", ""},
                {"INFO",  "Informatique",                "2", "SECONDAIRE", ""},
                {"ESP",   "Espagnol",                    "2", "SECONDAIRE", ""},
                {"EPS",   "Éducation Physique",          "1", "SECONDAIRE", ""},
                {"PHILO", "Philosophie",                 "3", "SECONDAIRE", ""},
                {"ECO",   "Économie",                    "2", "SECONDAIRE", ""},
        };
        for (String[] d : data) {
            if (!matiereRepo.existsByCode(d[0])) {
                Matiere m = Matiere.builder()
                        .code(d[0])
                        .nom(d[1])
                        .coefficient(Integer.parseInt(d[2]))
                        .niveauEnseignement(NiveauEnseignement.valueOf(d[3]))
                        .build();
                matiereRepo.save(m);
                log.info("  ✔ Matière : {} (coeff {})", d[1], d[2]);
            }
        }
    }


    private void creerClasses(Etablissement etab, AnneeScolaire annee) {
        String[][] data = {
                {"6A",   "Sixième A",       "SECONDAIRE"},
                {"5A",   "Cinquième A",     "SECONDAIRE"},
                {"4A",   "Quatrième A",     "SECONDAIRE"},
                {"3A",   "Troisième A",     "SECONDAIRE"},
                {"2ndC", "Seconde C",       "SECONDAIRE"},
                {"1ereC","Première C",      "SECONDAIRE"},
                {"TleC", "Terminale C",     "SECONDAIRE"},
        };
        for (String[] d : data) {
            boolean existe = classeRepo
                    .findByEtablissementIdAndAnneeScolaireId(etab.getId(), annee.getId())
                    .stream()
                    .anyMatch(c -> c.getCode().equals(d[0]));

            if (!existe) {
                ClasseScolaire c = ClasseScolaire.builder()
                        .code(d[0])
                        .nom(d[1])
                        .niveauEnseignement(NiveauEnseignement.valueOf(d[2]))
                        .capaciteMax(50)
                        .anneeScolaire(annee)
                        .etablissement(etab)
                        .build();
                classeRepo.save(c);
                log.info("  ✔ Classe : {}", d[1]);
            }
        }
    }


    private void creerEnseignants(Etablissement etab) {
        if (!enseignantRepo.existsByMatricule("ENS-001")) {
            Enseignant e = Enseignant.builder()
                    .matricule("ENS-001")
                    .nom("Martin")
                    .prenom("Jean")
                    .sexe(Sexe.MASCULIN)
                    .specialite("Mathématiques")
                    .grade("Professeur certifié")
                    .typeEnseignant(TypeEnseignant.TITULAIRE)
                    .dateEmbauche(LocalDate.of(2018, 9, 1))
                    .salaireBase(250000.0)
                    .etablissement(etab)
                    .actif(true)
                    .build();
            enseignantRepo.save(e);
            log.info("  ✔ Enseignant : {} {}", e.getNom(), e.getPrenom());
        }
    }

    private void creerEleves(Etablissement etab, AnneeScolaire annee) {
        if (!eleveRepo.existsByMatricule("ELV-2025-001")) {
            ClasseScolaire classe = classeRepo
                    .findByEtablissementIdAndAnneeScolaireId(etab.getId(), annee.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);

            Eleve el = Eleve.builder()
                    .matricule("ELV-2025-001")
                    .nom("Dupont")
                    .prenom("Marie")
                    .sexe(Sexe.FEMININ)
                    .dateNaissance(LocalDate.of(2010, 5, 15))
                    .lieuNaissance("Douala")
                    .nationalite("Camerounaise")
                    .nomParent("Dupont Pierre")
                    .telephoneParent("699000100")
                    .emailParent("dupont.pierre@gmail.com")
                    .statutEleve(StatutEleve.ACTIF)
                    .niveauEnseignement(NiveauEnseignement.SECONDAIRE)
                    .dateInscription(LocalDate.now())
                    .classeScolaire(classe)
                    .etablissement(etab)
                    .anneeScolaire(annee)
                    .build();
            eleveRepo.save(el);
            log.info("  ✔ Élève : {} {}", el.getNom(), el.getPrenom());
        }
    }
}