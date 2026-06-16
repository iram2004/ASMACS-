package asmacs.asmacs.maquette;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Maquette de présentation ASMACS — 5 portails / 30 écrans (coques fidèles au
 * design « ASMACS.dc.html », données mockées). Le shell visuel est fourni par
 * {@link MaquetteShell}, réutilisé par les modules réels (m01..m12).
 */
@Controller
@RequiredArgsConstructor
public class MaquetteController {

    private final MaquetteShell shell;

    @GetMapping("/maquette")
    public String index() {
        return "redirect:/maquette/public/home";
    }

    @GetMapping("/maquette/{role}/{view}")
    public String screen(@PathVariable String role, @PathVariable String view, Model model) {
        String active = role + "/" + view;
        if (!shell.isKnown(active)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Écran inconnu : " + active);
        }
        shell.apply(model, role, active);
        return "maquette/" + active;
    }
}
