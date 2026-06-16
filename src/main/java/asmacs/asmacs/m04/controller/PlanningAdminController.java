package asmacs.asmacs.m04.controller;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.m04.service.PlanningService;
import asmacs.asmacs.maquette.MaquetteShell;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** M04 — Classes & emplois du temps (vue administration). */
@Controller
@RequestMapping("/m04/admin")
@RequiredArgsConstructor
public class PlanningAdminController {

    private final PlanningService service;
    private final MaquetteShell shell;
    private final ClasseScolaireRepository classeRepo;

    @GetMapping("/edt")
    public String edt(@RequestParam(required = false) Long classe, Model model) {
        List<ClasseScolaire> classes = classeRepo.findAll();
        Long classeId = classe != null ? classe : defaultClasse(classes);

        shell.apply(model, "admin", "admin/timetable");
        model.addAttribute("edt", service.emploiClasse(classeId));
        model.addAttribute("classes", classes);
        model.addAttribute("selClasse", classeId);
        return "m04/admin/edt";
    }

    static Long defaultClasse(List<ClasseScolaire> classes) {
        return classes.stream().filter(c -> "3A".equals(c.getCode())).map(ClasseScolaire::getId)
                .findFirst().orElseGet(() -> classes.isEmpty() ? null : classes.get(0).getId());
    }
}
