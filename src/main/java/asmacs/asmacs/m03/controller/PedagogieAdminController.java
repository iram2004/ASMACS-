package asmacs.asmacs.m03.controller;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Matiere;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.core.repository.MatiereRepository;
import asmacs.asmacs.m03.service.PedagogieService;
import asmacs.asmacs.maquette.MaquetteShell;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** M03 — Relevé de notes & génération de bulletins (vue administration). */
@Controller
@RequestMapping("/m03/admin")
@RequiredArgsConstructor
public class PedagogieAdminController {

    private final PedagogieService service;
    private final MaquetteShell shell;
    private final ClasseScolaireRepository classeRepo;
    private final MatiereRepository matiereRepo;

    @GetMapping("/notes")
    public String notes(@RequestParam(required = false) Long classe,
                        @RequestParam(required = false) Long matiere,
                        @RequestParam(defaultValue = "1") Integer sequence,
                        Model model) {
        List<ClasseScolaire> classes = classeRepo.findAll();
        List<Matiere> matieres = matiereRepo.findAll();
        Long classeId = classe != null ? classe : defaultClasse(classes);
        Long matiereId = matiere != null ? matiere : defaultMatiere(matieres);

        shell.apply(model, "admin", "admin/grades");
        model.addAttribute("releve", service.releve(classeId, matiereId, sequence));
        model.addAttribute("classes", classes);
        model.addAttribute("matieres", matieres);
        model.addAttribute("sequence", sequence);
        model.addAttribute("selClasse", classeId);
        model.addAttribute("selMatiere", matiereId);
        return "m03/admin/notes";
    }

    static Long defaultClasse(List<ClasseScolaire> classes) {
        return classes.stream().filter(c -> "3A".equals(c.getCode())).map(ClasseScolaire::getId)
                .findFirst().orElseGet(() -> classes.isEmpty() ? null : classes.get(0).getId());
    }

    static Long defaultMatiere(List<Matiere> matieres) {
        return matieres.stream().filter(m -> "MATH".equals(m.getCode())).map(Matiere::getId)
                .findFirst().orElseGet(() -> matieres.isEmpty() ? null : matieres.get(0).getId());
    }
}
