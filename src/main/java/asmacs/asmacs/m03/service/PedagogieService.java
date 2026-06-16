package asmacs.asmacs.m03.service;

import asmacs.asmacs.m03.dto.BulletinVueDTO;
import asmacs.asmacs.m03.dto.ReleveVueDTO;

import java.util.Map;

/** M03 — Calculs pédagogiques : relevés de notes, bulletins, saisie. */
public interface PedagogieService {

    /** Relevé d'une classe pour une matière + stats (moyenne classe, réussite, distribution). */
    ReleveVueDTO releve(Long classeId, Long matiereId, Integer sequence);

    /** Bulletin d'un élève (toutes matières de sa classe) pour une séquence. */
    BulletinVueDTO bulletin(Long eleveId, Integer sequence);

    /**
     * Persiste les notes saisies par l'enseignant.
     * @param notes clés au format {@code note_{eleveId}_{TYPE}} (TYPE = INTERRO|DEVOIR|COMPO).
     */
    void saveSaisie(Long classeId, Long matiereId, Integer sequence, Long enseignantId, Map<String, String> notes);
}
