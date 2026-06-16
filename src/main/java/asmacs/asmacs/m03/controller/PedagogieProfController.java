package asmacs.asmacs.m03.controller;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.entity.Matiere;
import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.core.repository.EnseignantRepository;
import asmacs.asmacs.core.repository.MatiereRepository;
import asmacs.asmacs.m03.service.PedagogieService;
import asmacs.asmacs.maquette.MaquetteShell;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/** M03 — Saisie des notes (vue enseignant). */
@Controller
@RequestMapping("/m03/prof")
@RequiredArgsConstructor
public class PedagogieProfController {

    private final PedagogieService service;
    private final MaquetteShell shell;
    private final ClasseScolaireRepository classeRepo;
    private final MatiereRepository matiereRepo;
    private final EnseignantRepository enseignantRepo;

    @GetMapping("/notes")
    public String saisie(@RequestParam(required = false) Long classe,
                         @RequestParam(required = false) Long matiere,
                         @RequestParam(defaultValue = "1") Integer sequence,
                         Model model) {
        List<ClasseScolaire> classes = classeRepo.findAll();
        List<Matiere> matieres = matiereRepo.findAll();
        Long classeId = classe != null ? classe : PedagogieAdminController.defaultClasse(classes);
        Long matiereId = matiere != null ? matiere : PedagogieAdminController.defaultMatiere(matieres);

        shell.apply(model, "teacher", "teacher/grades");
        model.addAttribute("releve", service.releve(classeId, matiereId, sequence));
        model.addAttribute("classes", classes);
        model.addAttribute("matieres", matieres);
        model.addAttribute("sequence", sequence);
        model.addAttribute("selClasse", classeId);
        model.addAttribute("selMatiere", matiereId);
        return "m03/prof/notes";
    }

    @PostMapping("/notes")
    public String save(@RequestParam Long classe,
                       @RequestParam Long matiere,
                       @RequestParam(defaultValue = "1") Integer sequence,
                       @RequestParam Map<String, String> params) {
        Long ensId = enseignantRepo.findAll().stream().findFirst().map(e -> e.getId()).orElse(null);
        service.saveSaisie(classe, matiere, sequence, ensId, params);
        return "redirect:/m03/prof/notes?classe=" + classe + "&matiere=" + matiere + "&sequence=" + sequence;
    }
}
