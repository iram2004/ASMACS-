package asmacs.asmacs.m04.controller;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.m04.service.PlanningService;
import asmacs.asmacs.maquette.MaquetteShell;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** M04 — Emploi du temps de la classe de l'élève (vue élève / parent). */
@Controller
@RequestMapping("/m04/eleve")
@RequiredArgsConstructor
public class PlanningEleveController {

    private final PlanningService service;
    private final MaquetteShell shell;
    private final ClasseScolaireRepository classeRepo;

    @GetMapping("/edt")
    public String edt(@RequestParam(required = false) Long classe, Model model) {
        List<ClasseScolaire> classes = classeRepo.findAll();
        Long classeId = classe != null ? classe : PlanningAdminController.defaultClasse(classes);
        if (classeId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune classe disponible");
        }
        shell.apply(model, "student", "student/timetable");
        model.addAttribute("edt", service.emploiClasse(classeId));
        return "m04/eleve/edt";
    }
}
