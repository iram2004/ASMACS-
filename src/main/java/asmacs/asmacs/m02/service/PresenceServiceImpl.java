package asmacs.asmacs.m02.service;

import asmacs.asmacs.core.entity.Eleve;
import asmacs.asmacs.core.repository.*;
import asmacs.asmacs.m02.dto.PresenceDTO;
import asmacs.asmacs.m02.dto.SeanceDTO;
import asmacs.asmacs.m02.dto.StatPresenceDTO;
import asmacs.asmacs.m02.entity.AlerteFraude;
import asmacs.asmacs.m02.entity.Presence;
import asmacs.asmacs.m02.entity.Seance;
import asmacs.asmacs.m02.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PresenceServiceImpl implements PresenceService {

    private final SeanceRepository     seanceRepo;
    private final PresenceRepository   presenceRepo;
    private final AlerteFraudeRepository alerteRepo;
    private final EleveRepository      eleveRepo;
    private final ClasseScolaireRepository classeRepo;
    private final EnseignantRepository enseignantRepo;
    private final MatiereRepository    matiereRepo;
    private final AnneeScolaireRepository anneeRepo;

    // ── SÉANCES ────────────────────────────────────────────

    @Override
    public SeanceDTO creerSeance(SeanceDTO dto) {
        Seance s = Seance.builder()
                .dateSeance(dto.getDateSeance())
                .heureDebut(dto.getHeureDebut())
                .heureFin(dto.getHeureFin())
                .typeSeance(dto.getTypeSeance() != null ? dto.getTypeSeance() : "COURS")
                .salle(dto.getSalle())
                .geofencingActif(dto.getGeofencingActif() != null
                        ? dto.getGeofencingActif() : false)
                .latitudeAutorisee(dto.getLatitudeAutorisee())
                .longitudeAutorisee(dto.getLongitudeAutorisee())
                .rayonAutorisationMetres(dto.getRayonAutorisationMetres() != null
                        ? dto.getRayonAutorisationMetres() : 200)
                .statut("PLANIFIEE")
                .classe(classeRepo.findById(dto.getClasseId()).orElseThrow())
                .build();

        if (dto.getMatiereId() != null)
            s.setMatiere(matiereRepo.findById(dto.getMatiereId()).orElse(null));
        if (dto.getEnseignantId() != null)
            s.setEnseignant(enseignantRepo.findById(dto.getEnseignantId()).orElse(null));
        anneeRepo.findByEnCoursTrue().ifPresent(s::setAnneeScolaire);

        seanceRepo.save(s);
        log.info("M02 — Séance créée : {} {}", s.getDateSeance(), s.getHeureDebut());
        return toSeanceDTO(s);
    }

    @Override
    @Transactional(readOnly = true)
    public SeanceDTO getSeanceById(Long id) {
        return toSeanceDTO(seanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance introuvable")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceDTO> getSeancesEnCours() {
        return seanceRepo.findEnCours().stream()
                .map(this::toSeanceDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceDTO> getSeancesByDate(LocalDate date) {
        return seanceRepo.findByDateSeance(date).stream()
                .map(this::toSeanceDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceDTO> getSeancesByClasse(Long classeId) {
        return seanceRepo.findByClasseId(classeId).stream()
                .map(this::toSeanceDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeanceDTO> getSeancesByEnseignant(Long enseignantId) {
        return seanceRepo.findByEnseignantOrderByDate(enseignantId).stream()
                .map(this::toSeanceDTO).collect(Collectors.toList());
    }

    @Override
    public SeanceDTO demarrerSeance(Long seanceId) {
        Seance s = seanceRepo.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        s.setStatut("EN_COURS");
        s.setQrCodeToken(genererToken());
        seanceRepo.save(s);

        // Initialiser les présences à ABSENT pour tous les élèves
        List<Eleve> eleves = eleveRepo.findByClasseScolaireId(s.getClasse().getId());
        for (Eleve e : eleves) {
            if (!presenceRepo.existsByEleveIdAndSeanceId(e.getId(), seanceId)) {
                Presence p = Presence.builder()
                        .eleve(e).seance(s).statut("ABSENT").build();
                presenceRepo.save(p);
            }
        }
        log.info("M02 — Séance démarrée : {}, QR: {}", seanceId, s.getQrCodeToken());
        return toSeanceDTO(s);
    }

    @Override
    public SeanceDTO terminerSeance(Long seanceId) {
        Seance s = seanceRepo.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        s.setStatut("TERMINEE");
        seanceRepo.save(s);
        return toSeanceDTO(s);
    }

    @Override
    public String genererQRCode(Long seanceId) {
        Seance s = seanceRepo.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        String token = genererToken();
        s.setQrCodeToken(token);
        seanceRepo.save(s);
        return token;
    }

    // ── PRÉSENCES ──────────────────────────────────────────

    @Override
    public PresenceDTO marquerPresence(Long seanceId, Long eleveId,
                                       String methode,
                                       Double lat, Double lng) {
        Seance s = seanceRepo.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        Eleve e = eleveRepo.findById(eleveId)
                .orElseThrow(() -> new RuntimeException("Élève introuvable"));

        // Vérification géofencing
        boolean fraudeGPS = false;
        if (Boolean.TRUE.equals(s.getGeofencingActif())
                && lat != null && lng != null
                && s.getLatitudeAutorisee() != null) {
            double distance = calculerDistance(
                    lat, lng,
                    s.getLatitudeAutorisee(), s.getLongitudeAutorisee());
            if (distance > s.getRayonAutorisationMetres()) {
                fraudeGPS = true;
                enregistrerAlerte("GPS_INVALIDE",
                        "Pointage hors périmètre autorisé (" +
                                (int)distance + "m)", eleveId, seanceId, lat, lng);
            }
        }

        Optional<Presence> existing =
                presenceRepo.findByEleveIdAndSeanceId(eleveId, seanceId);
        Presence p = existing.orElse(Presence.builder().eleve(e).seance(s).build());

        p.setStatut(fraudeGPS ? "ABSENT" : "PRESENT");
        p.setMethodeValidation(methode);
        p.setHeureArrivee(LocalDateTime.now());
        p.setLatitudePointage(lat);
        p.setLongitudePointage(lng);
        p.setFraudeDetectee(fraudeGPS);
        presenceRepo.save(p);

        log.info("M02 — Présence : élève {} → {}, fraude={}",
                eleveId, p.getStatut(), fraudeGPS);
        return toPresenceDTO(p);
    }

    @Override
    public PresenceDTO marquerParQRCode(String token, Long eleveId,
                                        Double lat, Double lng) {
        Seance s = seanceRepo.findByQrCodeToken(token)
                .orElseThrow(() -> new RuntimeException("QR Code invalide"));

        // Vérifier si déjà scanné (fraude QR double)
        if (presenceRepo.existsByEleveIdAndSeanceId(eleveId, s.getId())) {
            Presence existing = presenceRepo
                    .findByEleveIdAndSeanceId(eleveId, s.getId()).get();
            if ("PRESENT".equals(existing.getStatut())) {
                enregistrerAlerte("QR_DOUBLE",
                        "Tentative de double scan QR Code",
                        eleveId, s.getId(), lat, lng);
                existing.setFraudeDetectee(true);
                presenceRepo.save(existing);
                return toPresenceDTO(existing);
            }
        }

        return marquerPresence(s.getId(), eleveId, "QR_CODE", lat, lng);
    }

    @Override
    public PresenceDTO modifierPresence(Long presenceId, String statut,
                                        String motif) {
        Presence p = presenceRepo.findById(presenceId)
                .orElseThrow(() -> new RuntimeException("Présence introuvable"));
        p.setStatut(statut);
        if (motif != null) p.setMotifAbsence(motif);
        if ("ABSENT".equals(statut) && motif != null) {
            p.setJustifiee(true);
        }
        presenceRepo.save(p);
        return toPresenceDTO(p);
    }

    @Override
    public PresenceDTO justifierAbsence(Long presenceId, String motif) {
        Presence p = presenceRepo.findById(presenceId)
                .orElseThrow(() -> new RuntimeException("Présence introuvable"));
        p.setJustifiee(true);
        p.setMotifAbsence(motif);
        presenceRepo.save(p);
        return toPresenceDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresenceDTO> getPresencesBySeance(Long seanceId) {
        return presenceRepo.findBySeanceId(seanceId).stream()
                .map(this::toPresenceDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresenceDTO> getPresencesByEleve(Long eleveId) {
        return presenceRepo.findByEleveOrderByDate(eleveId).stream()
                .map(this::toPresenceDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresenceDTO> getFraudes() {
        return presenceRepo.findByFraudeDetecteeTrue().stream()
                .map(this::toPresenceDTO).collect(Collectors.toList());
    }

    // ── STATISTIQUES ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StatPresenceDTO getStatsByClasse(Long classeId) {
        List<Presence> ps = presenceRepo.findByClasseAndStatut(classeId, "PRESENT");
        long presents  = presenceRepo.findByClasseAndStatut(classeId,"PRESENT").size();
        long absents   = presenceRepo.findByClasseAndStatut(classeId,"ABSENT").size();
        long retards   = presenceRepo.findByClasseAndStatut(classeId,"RETARD").size();
        long total     = presents + absents + retards;
        long fraudes   = presenceRepo.findByFraudeDetecteeTrue().stream()
                .filter(p -> classeId.equals(p.getSeance().getClasse().getId()))
                .count();
        return StatPresenceDTO.builder()
                .nbPresents(presents).nbAbsents(absents)
                .nbRetards(retards).nbTotal(total).nbFraudes(fraudes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StatPresenceDTO getStatsByEleve(Long eleveId) {
        long presents  = presenceRepo.countByEleveAndStatut(eleveId,"PRESENT");
        long absents   = presenceRepo.countByEleveAndStatut(eleveId,"ABSENT");
        long retards   = presenceRepo.countByEleveAndStatut(eleveId,"RETARD");
        long total     = presents + absents + retards;
        return StatPresenceDTO.builder()
                .nbPresents(presents).nbAbsents(absents)
                .nbRetards(retards).nbTotal(total)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAbsentsFrequents() {
        return presenceRepo.findAbsentsFrequents().stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    Long eleveId = (Long) row[0];
                    Long count   = (Long) row[1];
                    eleveRepo.findById(eleveId).ifPresent(e -> {
                        m.put("eleve", e.getNom() + " " + e.getPrenom());
                        m.put("matricule", e.getMatricule());
                        m.put("nbAbsences", count);
                        m.put("eleveId", eleveId);
                    });
                    return m;
                })
                .filter(m -> !m.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countPresentsAujourdhui() {
        return presenceRepo.findByDateBetween(LocalDate.now(), LocalDate.now())
                .stream().filter(p -> "PRESENT".equals(p.getStatut())).count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAbsentsAujourdhui() {
        return presenceRepo.findByDateBetween(LocalDate.now(), LocalDate.now())
                .stream().filter(p -> "ABSENT".equals(p.getStatut())).count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countFraudes() {
        return presenceRepo.findByFraudeDetecteeTrue().size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countSeancesEnCours() {
        return seanceRepo.countByStatut("EN_COURS");
    }

    // ── UTILITAIRES ────────────────────────────────────────

    private String genererToken() {
        return UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase();
    }

    private double calculerDistance(double lat1, double lon1,
                                    double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private void enregistrerAlerte(String type, String desc,
                                   Long eleveId, Long seanceId,
                                   Double lat, Double lng) {
        AlerteFraude alerte = AlerteFraude.builder()
                .typeAlerte(type)
                .description(desc)
                .latitudeTentative(lat)
                .longitudeTentative(lng)
                .statut("NOUVELLE")
                .build();
        if (eleveId != null)
            eleveRepo.findById(eleveId).ifPresent(alerte::setEleve);
        if (seanceId != null)
            seanceRepo.findById(seanceId).ifPresent(alerte::setSeance);
        alerteRepo.save(alerte);
    }

    // ── MAPPINGS ───────────────────────────────────────────

    private SeanceDTO toSeanceDTO(Seance s) {
        long nbP = presenceRepo.countBySeanceIdAndStatut(s.getId(),"PRESENT");
        long nbA = presenceRepo.countBySeanceIdAndStatut(s.getId(),"ABSENT");
        long nbR = presenceRepo.countBySeanceIdAndStatut(s.getId(),"RETARD");
        long nbF = presenceRepo.findByFraudeDetecteeTrue().stream()
                .filter(p -> s.getId().equals(p.getSeance().getId())).count();

        return SeanceDTO.builder()
                .id(s.getId())
                .dateSeance(s.getDateSeance())
                .heureDebut(s.getHeureDebut())
                .heureFin(s.getHeureFin())
                .typeSeance(s.getTypeSeance())
                .statut(s.getStatut())
                .salle(s.getSalle())
                .qrCodeToken(s.getQrCodeToken())
                .geofencingActif(s.getGeofencingActif())
                .latitudeAutorisee(s.getLatitudeAutorisee())
                .longitudeAutorisee(s.getLongitudeAutorisee())
                .rayonAutorisationMetres(s.getRayonAutorisationMetres())
                .classeId(s.getClasse().getId())
                .classeNom(s.getClasse().getNom())
                .matiereId(s.getMatiere() != null ? s.getMatiere().getId() : null)
                .matiereNom(s.getMatiere() != null ? s.getMatiere().getNom() : "—")
                .enseignantId(s.getEnseignant() != null ? s.getEnseignant().getId() : null)
                .enseignantNom(s.getEnseignant() != null
                        ? s.getEnseignant().getNom() + " " + s.getEnseignant().getPrenom() : "—")
                .anneeScolaireLibelle(s.getAnneeScolaire() != null
                        ? s.getAnneeScolaire().getLibelle() : null)
                .nbPresents(nbP).nbAbsents(nbA).nbRetards(nbR)
                .nbTotal(nbP + nbA + nbR).nbFraudes(nbF)
                .build();
    }

    private PresenceDTO toPresenceDTO(Presence p) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter hfmt = DateTimeFormatter.ofPattern("HH:mm");
        return PresenceDTO.builder()
                .id(p.getId())
                .statut(p.getStatut())
                .methodeValidation(p.getMethodeValidation())
                .heureArrivee(p.getHeureArrivee())
                .motifAbsence(p.getMotifAbsence())
                .justifiee(p.getJustifiee())
                .fraudeDetectee(p.getFraudeDetectee())
                .scoreFacial(p.getScoreFacial())
                .noteObservation(p.getNoteObservation())
                .eleveId(p.getEleve().getId())
                .eleveNom(p.getEleve().getNom())
                .elevePrenom(p.getEleve().getPrenom())
                .eleveMatricule(p.getEleve().getMatricule())
                .seanceId(p.getSeance().getId())
                .matiereNom(p.getSeance().getMatiere() != null
                        ? p.getSeance().getMatiere().getNom() : "—")
                .classeNom(p.getSeance().getClasse().getNom())
                .enseignantNom(p.getSeance().getEnseignant() != null
                        ? p.getSeance().getEnseignant().getNom() : "—")
                .dateSeanceFormatee(p.getSeance().getDateSeance().format(fmt))
                .heureDebutFormatee(p.getSeance().getHeureDebut().format(hfmt))
                .build();
    }
}