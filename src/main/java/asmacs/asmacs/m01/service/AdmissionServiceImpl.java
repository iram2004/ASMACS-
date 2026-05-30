package asmacs.asmacs.m01.service;

import asmacs.asmacs.core.entity.*;
import asmacs.asmacs.core.enums.Role;
import asmacs.asmacs.core.enums.StatutEleve;
import asmacs.asmacs.core.repository.*;
import asmacs.asmacs.m01.dto.CandidatureDTO;
import asmacs.asmacs.m01.dto.CandidatureFormDTO;
import asmacs.asmacs.m01.dto.PaiementDTO;
import asmacs.asmacs.m01.entity.Candidature;
import asmacs.asmacs.m01.entity.DocumentCandidat;
import asmacs.asmacs.m01.entity.PaiementAdmission;
import asmacs.asmacs.m01.repository.CandidatureRepository;
import asmacs.asmacs.m01.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdmissionServiceImpl implements AdmissionService {

    private final CandidatureRepository    candidatureRepo;
    private final DocumentRepository       documentRepo;
    private final EtablissementRepository  etablissementRepo;
    private final AnneeScolaireRepository  anneeScolaireRepo;
    private final ClasseScolaireRepository classeRepo;
    private final UtilisateurRepository    utilisateurRepo;
    private final EleveRepository          eleveRepo;
    private final PasswordEncoder          passwordEncoder;

    // Dossier de stockage des fichiers uploadés
    private static final String UPLOAD_DIR = "uploads/candidatures/";

    // ─────────────────────────────────────────────────────────
    // SOUMETTRE CANDIDATURE (côté candidat public)
    // ─────────────────────────────────────────────────────────
    @Override
    public CandidatureDTO soumettreCandidature(CandidatureFormDTO form) {
        log.info("M01 — Nouvelle candidature : {} {}", form.getNom(), form.getPrenom());

        // Vérifier doublon email
        if (candidatureRepo.existsByEmail(form.getEmail())) {
            throw new RuntimeException(
                    "Une candidature existe déjà avec cet email : " + form.getEmail());
        }

        Etablissement etab = etablissementRepo
                .findById(form.getEtablissementId())
                .orElseThrow(() -> new RuntimeException("Établissement introuvable"));

        AnneeScolaire annee = anneeScolaireRepo
                .findByEnCoursTrue()
                .orElse(null);

        // Créer la candidature
        Candidature c = Candidature.builder()
                .numeroCandidature(genererNumero())
                .nom(form.getNom().toUpperCase().trim())
                .prenom(capitaliser(form.getPrenom()))
                .sexe(form.getSexe())
                .dateNaissance(form.getDateNaissance())
                .lieuNaissance(form.getLieuNaissance())
                .nationalite(form.getNationalite() != null
                        ? form.getNationalite() : "Camerounaise")
                .telephone(form.getTelephone())
                .email(form.getEmail().toLowerCase().trim())
                .adresse(form.getAdresse())
                .nomParent(form.getNomParent())
                .telephoneParent(form.getTelephoneParent())
                .emailParent(form.getEmailParent())
                .niveauDemande(form.getNiveauDemande())
                .filiereDemandee(form.getFiliereDemandee())
                .etablissementPrecedent(form.getEtablissementPrecedent())
                .derniereClasse(form.getDerniereClasse())
                .moyennePrecedente(form.getMoyennePrecedente())
                .statut("EN_ATTENTE")
                .etablissement(etab)
                .anneeScolaire(annee)
                .build();

        candidatureRepo.save(c);

        // Sauvegarder les documents
        sauvegarderDocument(c, form.getBulletin(),       "BULLETIN");
        sauvegarderDocument(c, form.getActeNaissance(),  "ACTE_NAISSANCE");
        sauvegarderDocument(c, form.getPhoto(),          "PHOTO");
        sauvegarderDocument(c, form.getDiplome(),        "DIPLOME");
        sauvegarderDocument(c, form.getCniParent(),      "CNI_PARENT");

        log.info("  ✔ Candidature créée : {}", c.getNumeroCandidature());
        return toDTO(c);
    }

    // ─────────────────────────────────────────────────────────
    // ENREGISTRER PAIEMENT
    // ─────────────────────────────────────────────────────────
    @Override
    public CandidatureDTO enregistrerPaiement(Long candidatureId, PaiementDTO dto) {
        Candidature c = findById(candidatureId);

        PaiementAdmission paiement = PaiementAdmission.builder()
                .operateur(dto.getOperateur())
                .numeroTelephone(dto.getNumeroTelephone())
                .referenceTransaction(dto.getReferenceTransaction())
                .montant(dto.getMontant() != null ? dto.getMontant() : 25000.0)
                .statut("EN_ATTENTE")
                .candidature(c)
                .build();

        c.setStatut("PAIEMENT_ATTENTE");
        candidatureRepo.save(c);

        log.info("M01 — Paiement enregistré pour : {}", c.getNumeroCandidature());
        return toDTO(c);
    }

    // ─────────────────────────────────────────────────────────
    // SOUMETTRE TEST (réponses QCM)
    // ─────────────────────────────────────────────────────────
    @Override
    public CandidatureDTO soumettreTest(Long candidatureId, int[] reponses) {
        Candidature c = findById(candidatureId);

        // Correction simple : réponses correctes prédéfinies
        int[] corriges = {1, 2, 1, 3, 2, 1, 4, 2, 3, 1};
        int score = 0;
        for (int i = 0; i < Math.min(reponses.length, corriges.length); i++) {
            if (reponses[i] == corriges[i]) score++;
        }
        int scoreFinal = (score * 100) / corriges.length;

        c.setScoreTest(scoreFinal);
        c.setTestEffectue(true);
        c.setDateTest(LocalDateTime.now());
        c.setStatut("TEST_EFFECTUE");

        candidatureRepo.save(c);
        log.info("M01 — Test soumis, score : {}%", scoreFinal);
        return toDTO(c);
    }

    // ─────────────────────────────────────────────────────────
    // VALIDER (ADMIN)
    // ─────────────────────────────────────────────────────────
    @Override
    public CandidatureDTO valider(Long id, Long classeId, String adminUsername) {
        Candidature c = findById(id);

        ClasseScolaire classe = null;
        if (classeId != null) {
            classe = classeRepo.findById(classeId).orElse(null);
        }

        // 1. Générer matricule
        String matricule = genererMatricule(c);
        c.setMatriculeGenere(matricule);

        // 2. Créer compte utilisateur étudiant
        String username = genererUsername(c);
        String password = genererPassword();

        Utilisateur user = Utilisateur.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .email(c.getEmail())
                .telephone(c.getTelephone())
                .role(Role.ELEVE)
                .etablissement(c.getEtablissement())
                .actif(true)
                .build();
        utilisateurRepo.save(user);

        // 3. Créer l'élève
        Eleve eleve = Eleve.builder()
                .matricule(matricule)
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .sexe(c.getSexe())
                .dateNaissance(c.getDateNaissance())
                .lieuNaissance(c.getLieuNaissance())
                .telephone(c.getTelephone())
                .email(c.getEmail())
                .nomParent(c.getNomParent())
                .telephoneParent(c.getTelephoneParent())
                .emailParent(c.getEmailParent())
                .niveauEnseignement(c.getNiveauDemande())
                .statutEleve(StatutEleve.ACTIF)
                .classeScolaire(classe)
                .etablissement(c.getEtablissement())
                .anneeScolaire(c.getAnneeScolaire())
                .utilisateur(user)
                .build();
        eleveRepo.save(eleve);

        // 4. Mettre à jour candidature
        c.setStatut("VALIDE");
        c.setClasseAffectee(classe);
        c.setUtilisateurCree(user);
        c.setDateValidation(LocalDateTime.now());
        candidatureRepo.save(c);

        log.info("M01 ✔ Candidature validée → matricule: {}, login: {}/{}",
                matricule, username, password);
        return toDTO(c);
    }

    // ─────────────────────────────────────────────────────────
    // REJETER (ADMIN)
    // ─────────────────────────────────────────────────────────
    @Override
    public CandidatureDTO rejeter(Long id, String motif) {
        Candidature c = findById(id);
        c.setStatut("REJETE");
        c.setMotifRejet(motif);
        c.setDateRejet(LocalDateTime.now());
        candidatureRepo.save(c);
        log.info("M01 — Candidature rejetée : {}", id);
        return toDTO(c);
    }

    // ─────────────────────────────────────────────────────────
    // DEMANDER CORRECTION (ADMIN)
    // ─────────────────────────────────────────────────────────
    @Override
    public CandidatureDTO demanderCorrection(Long id, String message) {
        Candidature c = findById(id);
        c.setStatut("DOCUMENTS_REQUIS");
        c.setObservations(message);
        candidatureRepo.save(c);
        return toDTO(c);
    }

    // ─────────────────────────────────────────────────────────
    // CONFIRMER PAIEMENT (ADMIN)
    // ─────────────────────────────────────────────────────────
    @Override
    public CandidatureDTO confirmerPaiement(Long id) {
        Candidature c = findById(id);
        if (c.getPaiement() != null) {
            c.getPaiement().setStatut("EFFECTUE");
            c.getPaiement().setDatePaiement(LocalDateTime.now());
        }
        c.setStatut("TEST_ATTENTE");
        candidatureRepo.save(c);
        return toDTO(c);
    }

    // ─────────────────────────────────────────────────────────
    // LECTURE
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CandidatureDTO getStatutByEmail(String email) {
        Candidature c = candidatureRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Aucune candidature pour : " + email));
        return toDTO(c);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatureDTO getById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatureDTO getByNumero(String numero) {
        return toDTO(candidatureRepo.findByNumeroCandidature(numero)
                .orElseThrow(() ->
                        new RuntimeException("Candidature introuvable : " + numero)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDTO> getAll() {
        return candidatureRepo.findAllByOrderByDateSoumissionDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDTO> getByStatut(String statut) {
        return candidatureRepo.findByStatutOrderByDateSoumissionDesc(statut)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDTO> search(String query) {
        return candidatureRepo.search(query)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatureDTO> getClassement() {
        return candidatureRepo.findAllOrderedByScore()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatut(String statut) {
        return candidatureRepo.countByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return candidatureRepo.count();
    }

    // ─────────────────────────────────────────────────────────
    // UTILITAIRES PRIVÉS
    // ─────────────────────────────────────────────────────────

    private Candidature findById(Long id) {
        return candidatureRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Candidature introuvable : " + id));
    }

    private String genererNumero() {
        long count = candidatureRepo.count() + 1;
        return String.format("CAND-%d-%04d", Year.now().getValue(), count);
    }

    private String genererMatricule(Candidature c) {
        long count = eleveRepo.count() + 1;
        return String.format("ETU-%d-%04d", Year.now().getValue(), count);
    }

    private String genererUsername(Candidature c) {
        String base = (c.getPrenom().charAt(0) + c.getNom())
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        String username = base;
        int i = 1;
        while (utilisateurRepo.existsByUsername(username)) {
            username = base + i++;
        }
        return username;
    }

    private String genererPassword() {
        return "Asmacs" + Year.now().getValue() + "@";
    }

    private String capitaliser(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase()
                + s.substring(1).toLowerCase();
    }

    private void sauvegarderDocument(Candidature c,
                                     MultipartFile fichier,
                                     String type) {
        if (fichier == null || fichier.isEmpty()) return;
        try {
            Path dossier = Paths.get(UPLOAD_DIR + c.getNumeroCandidature());
            Files.createDirectories(dossier);
            String nomFichier = type + "_" + fichier.getOriginalFilename();
            Path chemin = dossier.resolve(nomFichier);
            Files.copy(fichier.getInputStream(), chemin,
                    StandardCopyOption.REPLACE_EXISTING);

            DocumentCandidat doc = DocumentCandidat.builder()
                    .typeDocument(type)
                    .nomFichier(nomFichier)
                    .cheminFichier(chemin.toString())
                    .tailleFichier(fichier.getSize())
                    .typeMime(fichier.getContentType())
                    .candidature(c)
                    .build();
            documentRepo.save(doc);
        } catch (IOException e) {
            log.warn("Document non sauvegardé ({}) : {}", type, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // MAPPING → DTO
    // ─────────────────────────────────────────────────────────
    private CandidatureDTO toDTO(Candidature c) {
        List<String> docs = documentRepo
                .findByCandidatureId(c.getId())
                .stream()
                .map(DocumentCandidat::getTypeDocument)
                .collect(Collectors.toList());

        CandidatureDTO.CandidatureDTOBuilder b = CandidatureDTO.builder()
                .id(c.getId())
                .numeroCandidature(c.getNumeroCandidature())
                .statut(c.getStatut())
                .motifRejet(c.getMotifRejet())
                .observations(c.getObservations())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .sexe(c.getSexe())
                .dateNaissance(c.getDateNaissance())
                .lieuNaissance(c.getLieuNaissance())
                .telephone(c.getTelephone())
                .email(c.getEmail())
                .adresse(c.getAdresse())
                .nomParent(c.getNomParent())
                .telephoneParent(c.getTelephoneParent())
                .emailParent(c.getEmailParent())
                .niveauDemande(c.getNiveauDemande())
                .filiereDemandee(c.getFiliereDemandee())
                .etablissementPrecedent(c.getEtablissementPrecedent())
                .derniereClasse(c.getDerniereClasse())
                .moyennePrecedente(c.getMoyennePrecedente())
                .scoreTest(c.getScoreTest())
                .testEffectue(c.getTestEffectue())
                .dateTest(c.getDateTest())
                .matriculeGenere(c.getMatriculeGenere())
                .classeAffecteeNom(c.getClasseAffectee() != null
                        ? c.getClasseAffectee().getNom() : null)
                .dateValidation(c.getDateValidation())
                .etablissementNom(c.getEtablissement().getNom())
                .anneeScolaireLibelle(c.getAnneeScolaire() != null
                        ? c.getAnneeScolaire().getLibelle() : null)
                .dateSoumission(c.getDateSoumission())
                .dateModification(c.getDateModification())
                .typesDocumentsPresents(docs);

        if (c.getPaiement() != null) {
            b.paiementStatut(c.getPaiement().getStatut())
                    .paiementOperateur(c.getPaiement().getOperateur())
                    .paiementReference(c.getPaiement().getReferenceTransaction())
                    .paiementMontant(c.getPaiement().getMontant());
        }

        return b.build();
    }
}