package asmacs.asmacs.m01.controller;

import asmacs.asmacs.core.repository.ClasseScolaireRepository;
import asmacs.asmacs.m01.dto.CandidatureDTO;
import asmacs.asmacs.m01.service.AdmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/m01")
@RequiredArgsConstructor
@Slf4j
public class AdmissionAdminController {

    private final AdmissionService         admissionService;
    private final ClasseScolaireRepository classeRepo;

    // GET /m01 → liste candidatures
    @GetMapping
    public String liste(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String q,
            Model model) {

        List<CandidatureDTO> candidatures;

        if (q != null && !q.isBlank()) {
            candidatures = admissionService.search(q);
            model.addAttribute("q", q);
        } else if (statut != null && !statut.isBlank()) {
            candidatures = admissionService.getByStatut(statut);
            model.addAttribute("statutFiltre", statut);
        } else {
            candidatures = admissionService.getAll();
        }

        model.addAttribute("candidatures", candidatures);
        model.addAttribute("nbTotal",       admissionService.countTotal());
        model.addAttribute("nbAttente",     admissionService.countByStatut("EN_ATTENTE"));
        model.addAttribute("nbValides",     admissionService.countByStatut("VALIDE"));
        model.addAttribute("nbRejetes",     admissionService.countByStatut("REJETE"));
        model.addAttribute("nbPaiement",    admissionService.countByStatut("PAIEMENT_ATTENTE"));
        model.addAttribute("nbTest",        admissionService.countByStatut("TEST_EFFECTUE"));

        return "m01/admin/liste";
    }

    // GET /m01/{id} → détail
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("candidature",
                admissionService.getById(id));
        model.addAttribute("classes",
                classeRepo.findAll());
        return "m01/admin/detail";
    }

    // GET /m01/classement → classement par score
    @GetMapping("/classement")
    public String classement(Model model) {
        model.addAttribute("candidatures",
                admissionService.getClassement());
        return "m01/admin/classement";
    }

    // POST /m01/{id}/valider
    @PostMapping("/{id}/valider")
    public String valider(
            @PathVariable Long id,
            @RequestParam(required = false) Long classeId,
            Authentication auth,
            RedirectAttributes redirectAttrs) {
        try {
            CandidatureDTO result =
                    admissionService.valider(id, classeId, auth.getName());
            redirectAttrs.addFlashAttribute("succes",
                    "Candidature validée ! Matricule : "
                            + result.getMatriculeGenere());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/m01/" + id;
    }

    // POST /m01/{id}/rejeter
    @PostMapping("/{id}/rejeter")
    public String rejeter(
            @PathVariable Long id,
            @RequestParam String motif,
            RedirectAttributes redirectAttrs) {
        try {
            admissionService.rejeter(id, motif);
            redirectAttrs.addFlashAttribute("succes",
                    "Candidature rejetée.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/m01/" + id;
    }

    // POST /m01/{id}/correction
    @PostMapping("/{id}/correction")
    public String demanderCorrection(
            @PathVariable Long id,
            @RequestParam String message,
            RedirectAttributes redirectAttrs) {
        admissionService.demanderCorrection(id, message);
        redirectAttrs.addFlashAttribute("succes",
                "Demande de correction envoyée.");
        return "redirect:/m01/" + id;
    }

    // POST /m01/{id}/confirmer-paiement
    @PostMapping("/{id}/confirmer-paiement")
    public String confirmerPaiement(
            @PathVariable Long id,
            RedirectAttributes redirectAttrs) {
        admissionService.confirmerPaiement(id);
        redirectAttrs.addFlashAttribute("succes",
                "Paiement confirmé.");
        return "redirect:/m01/" + id;
    }
}