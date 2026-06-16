package asmacs.asmacs.m04.service;

import asmacs.asmacs.m04.dto.EmploiTempsVueDTO;

/** M04 — Construction de l'emploi du temps et détection des conflits. */
public interface PlanningService {

    /** Emploi du temps hebdomadaire d'une classe (grille créneaux × jours). */
    EmploiTempsVueDTO emploiClasse(Long classeId);
}
