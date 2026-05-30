package asmacs.asmacs.core.controller;

import asmacs.asmacs.core.enums.Role;
import asmacs.asmacs.core.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final EleveRepository          eleveRepo;
    private final EnseignantRepository     enseignantRepo;
    private final UtilisateurRepository    utilisateurRepo;
    private final AnneeScolaireRepository  anneeScolaireRepo;
    private final ClasseScolaireRepository classeRepo;

    @GetMapping("/")
    public String root(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return redirect(auth);
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return redirect(auth);
        }
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";

        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(g -> g.getAuthority().replace("ROLE_", ""))
                .orElse("INCONNU");

        // Redirige selon le rôle
        if ("ELEVE".equals(role) || "PARENT".equals(role)) {
            return dashboardEtudiant(auth, model);
        }
        return dashboardAdmin(auth, model, role);
    }

    // ─────────────────────────────────────────────────────────
    private String dashboardAdmin(Authentication auth, Model model, String role) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("role", role);
        model.addAttribute("totalEleves",      eleveRepo.count());
        model.addAttribute("totalEnseignants", enseignantRepo.count());
        model.addAttribute("totalClasses",     classeRepo.count());
        model.addAttribute("totalUtilisateurs",utilisateurRepo.count());
        anneeScolaireRepo.findByEnCoursTrue()
                .ifPresent(a -> model.addAttribute("anneeCourante", a));
        return "admin/dashboard";
    }

    private String dashboardEtudiant(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        utilisateurRepo.findByUsername(auth.getName())
                .ifPresent(u -> {
                    model.addAttribute("user", u);
                    eleveRepo.findAll().stream()
                            .filter(e -> e.getUtilisateur() != null
                                    && e.getUtilisateur().getId().equals(u.getId()))
                            .findFirst()
                            .ifPresent(e -> model.addAttribute("eleve", e));
                });
        return "etudiant/dashboard";
    }

    private String redirect(Authentication auth) {
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(g -> g.getAuthority().replace("ROLE_", ""))
                .orElse("");
        return "ELEVE".equals(role) || "PARENT".equals(role)
                ? "redirect:/dashboard"
                : "redirect:/dashboard";
    }
}