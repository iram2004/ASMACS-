package asmacs.asmacs.m02.controller;

import asmacs.asmacs.core.entity.Eleve;
import asmacs.asmacs.core.repository.*;
import asmacs.asmacs.m02.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/m02/etudiant")
@RequiredArgsConstructor
public class PresenceEleveController {

    private final PresenceService      presenceService;
    private final UtilisateurRepository utilisateurRepo;
    private final EleveRepository      eleveRepo;

    // GET /m02/etudiant/mes-presences
    @GetMapping("/mes-presences")
    public String mesPresences(Authentication auth, Model model) {
        utilisateurRepo.findByUsername(auth.getName()).ifPresent(u -> {
            Optional<Eleve> eleveOpt = eleveRepo.findAll().stream()
                    .filter(e -> e.getUtilisateur() != null
                            && e.getUtilisateur().getId().equals(u.getId()))
                    .findFirst();

            eleveOpt.ifPresent(eleve -> {
                model.addAttribute("eleve", eleve);
                model.addAttribute("presences",
                        presenceService.getPresencesByEleve(eleve.getId()));
                model.addAttribute("stats",
                        presenceService.getStatsByEleve(eleve.getId()));
            });
        });
        return "m02/etudiant/mes-presences";
    }

    // POST /m02/etudiant/scanner-qr
    @PostMapping("/scanner-qr")
    public String scannerQR(
            @RequestParam String token,
            Authentication auth,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            RedirectAttributes redirectAttrs) {

        utilisateurRepo.findByUsername(auth.getName()).ifPresent(u -> {
            eleveRepo.findAll().stream()
                    .filter(e -> e.getUtilisateur() != null
                            && e.getUtilisateur().getId().equals(u.getId()))
                    .findFirst()
                    .ifPresent(eleve -> {
                        try {
                            presenceService.marquerParQRCode(
                                    token, eleve.getId(), latitude, longitude);
                            redirectAttrs.addFlashAttribute("succes",
                                    "Présence enregistrée avec succès !");
                        } catch (Exception e2) {
                            redirectAttrs.addFlashAttribute("erreur",
                                    e2.getMessage());
                        }
                    });
        });
        return "redirect:/m02/etudiant/mes-presences";
    }

    // POST /m02/etudiant/justifier/{presenceId}
    @PostMapping("/justifier/{presenceId}")
    public String justifier(
            @PathVariable Long presenceId,
            @RequestParam String motif,
            RedirectAttributes redirectAttrs) {
        presenceService.justifierAbsence(presenceId, motif);
        redirectAttrs.addFlashAttribute("succes", "Justificatif enregistré");
        return "redirect:/m02/etudiant/mes-presences";
    }
}