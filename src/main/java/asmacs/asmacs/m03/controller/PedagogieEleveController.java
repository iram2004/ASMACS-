package asmacs.asmacs.m03.controller;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Eleve;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.core.repository.EleveRepository;
import asmacs.asmacs.m03.service.PedagogieService;
import asmacs.asmacs.maquette.MaquetteShell;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

/** M03 — Bulletin de l'élève (vue élève / parent). */
@Controller
@RequestMapping("/m03/eleve")
@RequiredArgsConstructor
public class PedagogieEleveController {

    private final PedagogieService service;
    private final MaquetteShell shell;
    private final EleveRepository eleveRepo;
    private final ClasseScolaireRepository classeRepo;

    @GetMapping("/bulletin")
    public String bulletin(@RequestParam(required = false) Long eleve,
                          @RequestParam(defaultValue = "1") Integer sequence,
                          Model model) {
        Long eleveId = eleve != null ? eleve : defaultEleve();
        if (eleveId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun élève disponible");
        }
        shell.apply(model, "student", "student/grades");
        model.addAttribute("bulletin", service.bulletin(eleveId, sequence));
        model.addAttribute("sequence", sequence);
        return "m03/eleve/bulletin";
    }

    private Long defaultEleve() {
        // Classe 3A (sans toucher aux relations lazy hors transaction)
        Long classe3a = classeRepo.findAll().stream()
                .filter(c -> "3A".equals(c.getCode()))
                .map(ClasseScolaire::getId).findFirst().orElse(null);
        if (classe3a != null) {
            List<Eleve> eleves = eleveRepo.findByClasseScolaireId(classe3a);
            if (!eleves.isEmpty()) return eleves.get(0).getId();
        }
        List<Eleve> all = eleveRepo.findAll();
        return all.isEmpty() ? null : all.get(0).getId();
    }
}
