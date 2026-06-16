package asmacs.asmacs.core.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Authentification et aiguillage post-connexion.
 *
 * Le formulaire de connexion ({@code auth/login}) et les espaces après
 * connexion sont rendus avec le système de design de la maquette : chaque rôle
 * est redirigé vers son portail {@code /maquette/{role}/...}.
 */
@Controller
public class AuthController {

    @GetMapping("/")
    public String root(Authentication auth) {
        return (auth != null && auth.isAuthenticated()) ? "redirect:/dashboard" : "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    /** Aiguille l'utilisateur connecté vers le portail correspondant à son rôle. */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(g -> g.getAuthority().replace("ROLE_", ""))
                .orElse("");
        return "redirect:/maquette/" + portalFor(role) + "/dashboard";
    }

    private String portalFor(String role) {
        return switch (role) {
            case "ELEVE" -> "student";
            case "PARENT" -> "parent";
            case "ENSEIGNANT" -> "teacher";
            default -> "admin"; // ADMIN, DIRECTEUR, COMPTABLE, DSI, SECRETAIRE, INSPECTEUR…
        };
    }
}
