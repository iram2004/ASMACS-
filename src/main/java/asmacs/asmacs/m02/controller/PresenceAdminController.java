package asmacs.asmacs.m02.controller;

import asmacs.asmacs.core.repository.*;
import asmacs.asmacs.m02.dto.SeanceDTO;
import asmacs.asmacs.m02.repository.AlerteFraudeRepository;
import asmacs.asmacs.m02.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/m02")
@RequiredArgsConstructor
public class PresenceAdminController {

    private final PresenceService          presenceService;
    private final ClasseScolaireRepository classeRepo;
    private final EnseignantRepository     enseignantRepo;
    private final MatiereRepository        matiereRepo;
    private final AlerteFraudeRepository   alerteRepo;
    private final EleveRepository          eleveRepo;

    // GET /m02 → Dashboard présences admin
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("seancesEnCours",
                presenceService.getSeancesEnCours());
        model.addAttribute("seancesAujourdhui",
                presenceService.getSeancesByDate(LocalDate.now()));
        model.addAttribute("nbPresents",
                presenceService.countPresentsAujourdhui());
        model.addAttribute("nbAbsents",
                presenceService.countAbsentsAujourdhui());
        model.addAttribute("nbFraudes",
                presenceService.countFraudes());
        model.addAttribute("nbSeancesEnCours",
                presenceService.countSeancesEnCours());
        model.addAttribute("alertes",
                alerteRepo.findByStatut("NOUVELLE"));
        model.addAttribute("absentsFrequents",
                presenceService.getAbsentsFrequents());
        return "m02/admin/dashboard";
    }

    // GET /m02/liste → liste des présences
    @GetMapping("/liste")
    public String liste(
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long enseignantId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<SeanceDTO> seances;
        if (classeId != null) {
            seances = presenceService.getSeancesByClasse(classeId);
        } else if (enseignantId != null) {
            seances = presenceService.getSeancesByEnseignant(enseignantId);
        } else if (date != null) {
            seances = presenceService.getSeancesByDate(date);
        } else {
            seances = presenceService.getSeancesByDate(LocalDate.now());
        }

        model.addAttribute("seances",       seances);
        model.addAttribute("classes",       classeRepo.findAll());
        model.addAttribute("enseignants",   enseignantRepo.findByActifTrue());
        model.addAttribute("classeId",      classeId);
        model.addAttribute("enseignantId",  enseignantId);
        model.addAttribute("statut",        statut);
        model.addAttribute("date",          date != null ? date : LocalDate.now());
        return "m02/admin/liste";
    }

    // GET /m02/seance/{id} → détail d'une séance
    @GetMapping("/seance/{id}")
    public String seanceDetail(@PathVariable Long id, Model model) {
        model.addAttribute("seance",    presenceService.getSeanceById(id));
        model.addAttribute("presences", presenceService.getPresencesBySeance(id));
        return "m02/admin/detail-seance";
    }

    // POST /m02/seance/creer
    @PostMapping("/seance/creer")
    public String creerSeance(
            @ModelAttribute SeanceDTO dto,
            RedirectAttributes redirectAttrs) {
        try {
            SeanceDTO created = presenceService.creerSeance(dto);
            redirectAttrs.addFlashAttribute("succes",
                    "Séance créée avec succès");
            return "redirect:/m02/seance/" + created.getId();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/m02";
        }
    }

    // POST /m02/seance/{id}/demarrer
    @PostMapping("/seance/{id}/demarrer")
    public String demarrer(@PathVariable Long id,
                           RedirectAttributes redirectAttrs) {
        try {
            presenceService.demarrerSeance(id);
            redirectAttrs.addFlashAttribute("succes",
                    "Séance démarrée — QR Code généré");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/m02/seance/" + id;
    }

    // POST /m02/seance/{id}/terminer
    @PostMapping("/seance/{id}/terminer")
    public String terminer(@PathVariable Long id,
                           RedirectAttributes redirectAttrs) {
        presenceService.terminerSeance(id);
        redirectAttrs.addFlashAttribute("succes", "Séance terminée");
        return "redirect:/m02/seance/" + id;
    }

    // POST /m02/presence/{id}/modifier
    @PostMapping("/presence/{id}/modifier")
    public String modifierPresence(
            @PathVariable Long id,
            @RequestParam String statut,
            @RequestParam(required = false) String motif,
            @RequestParam Long seanceId,
            RedirectAttributes redirectAttrs) {
        presenceService.modifierPresence(id, statut, motif);
        redirectAttrs.addFlashAttribute("succes", "Présence modifiée");
        return "redirect:/m02/seance/" + seanceId;
    }

    // GET /m02/fraudes → liste des fraudes
    @GetMapping("/fraudes")
    public String fraudes(Model model) {
        model.addAttribute("fraudes",  presenceService.getFraudes());
        model.addAttribute("alertes",  alerteRepo.findAllByOrderByDateAlerteDesc());
        return "m02/admin/fraudes";
    }

    // GET /m02/rapports
    @GetMapping("/rapports")
    public String rapports(Model model) {
        model.addAttribute("classes",  classeRepo.findAll());
        model.addAttribute("absentsFrequents",
                presenceService.getAbsentsFrequents());
        return "m02/admin/rapports";
    }

    // GET /m02/qrcode/{id}
    @GetMapping("/qrcode/{id}")
    public String qrcode(@PathVariable Long id, Model model) {
        model.addAttribute("seance", presenceService.getSeanceById(id));
        return "m02/admin/qrcode";
    }
}