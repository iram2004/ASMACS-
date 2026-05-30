package asmacs.asmacs.m02.controller;

import asmacs.asmacs.core.repository.*;
import asmacs.asmacs.m02.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/m02/prof")
@RequiredArgsConstructor
public class PresenceProfController {

    private final PresenceService       presenceService;
    private final EnseignantRepository  enseignantRepo;

    // GET /m02/prof → liste des cours du prof
    @GetMapping
    public String mesCours(Authentication auth, Model model) {
        enseignantRepo.searchByNomOuPrenomOuMatricule(auth.getName())
                .stream().findFirst()
                .ifPresent(ens -> {
                    model.addAttribute("seances",
                            presenceService.getSeancesByEnseignant(ens.getId()));
                    model.addAttribute("enseignant", ens);
                });
        return "m02/prof/liste-cours";
    }

    // GET /m02/prof/scanner/{seanceId}
    @GetMapping("/scanner/{seanceId}")
    public String scanner(@PathVariable Long seanceId, Model model) {
        model.addAttribute("seance",
                presenceService.getSeanceById(seanceId));
        model.addAttribute("presences",
                presenceService.getPresencesBySeance(seanceId));
        return "m02/prof/scanner";
    }

    // POST /m02/prof/marquer
    @PostMapping("/marquer")
    public String marquer(
            @RequestParam Long seanceId,
            @RequestParam Long eleveId,
            @RequestParam String methode,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            RedirectAttributes redirectAttrs) {
        try {
            presenceService.marquerPresence(
                    seanceId, eleveId, methode, latitude, longitude);
            redirectAttrs.addFlashAttribute("succes", "Présence enregistrée");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/m02/prof/scanner/" + seanceId;
    }

    // POST /m02/prof/modifier-presence
    @PostMapping("/modifier-presence")
    public String modifierPresence(
            @RequestParam Long presenceId,
            @RequestParam String statut,
            @RequestParam(required = false) String motif,
            @RequestParam Long seanceId,
            RedirectAttributes redirectAttrs) {
        presenceService.modifierPresence(presenceId, statut, motif);
        redirectAttrs.addFlashAttribute("succes", "Présence modifiée");
        return "redirect:/m02/prof/scanner/" + seanceId;
    }
}