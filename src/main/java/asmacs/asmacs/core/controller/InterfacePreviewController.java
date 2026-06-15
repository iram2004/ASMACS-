package asmacs.asmacs.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class InterfacePreviewController {

    private static final List<InterfacePage> PAGES = List.of(
            page("dashboard_global_admin_suite", "Dashboard global", "Administration"),
            page("pr_sence_anti_fraude_admin_suite", "Presence anti-fraude", "Administration"),
            page("gestion_p_dagogique_admin_suite", "Gestion pedagogique", "Administration"),
            page("rh_paie_admin_suite", "RH et paie", "Administration"),
            page("comptabilit_globale_admin_suite", "Comptabilite globale", "Administration"),
            page("configuration_syst_me_admin_suite", "Configuration systeme", "Administration"),
            page("gestion_des_admissions_admin_suite", "Gestion des admissions", "Administration"),
            page("console_ia_edubot_admin_suite", "Console IA EduBot", "Administration"),
            page("gestionnaire_e_learning_admin_suite", "Gestionnaire e-learning", "Administration"),
            page("biblioth_que_num_rique_admin_suite", "Bibliotheque numerique", "Administration"),
            page("mod_ration_du_r_seau_social_admin_suite", "Moderation reseau social", "Administration"),
            page("notifications_param_tres_admin_suite", "Notifications et profil", "Administration"),
            page("supervision_du_transport_admin_suite", "Supervision transport", "Administration"),
            page("suivi_maternelle_primaire_admin_suite", "Suivi maternelle primaire", "Administration"),
            page("tats_du_syst_me_erreurs_admin_suite", "Etats du systeme", "Administration"),
            page("mentions_l_gales_confidentialit_admin_suite", "Mentions legales", "Administration"),
            page("asmacs_excellence_ducative_100_camerounaise", "ASMACS excellence educative", "Public"),
            page("fonctionnalit_s_asmacs_suite", "Fonctionnalites ASMACS", "Public"),
            page("tarification_asmacs_suite", "Tarification ASMACS", "Public"),
            page("portail_d_admission_asmacs", "Portail admission", "Public"),
            page("contact_d_mo_asmacs_suite", "Contact et demo", "Public"),
            page("bienvenue_sur_asmacs_onboarding", "Bienvenue ASMACS", "Public"),
            page("connexion_s_curis_e_asmacs", "Connexion securisee", "Authentification"),
            page("r_cup_ration_de_mot_de_passe_asmacs", "Recuperation mot de passe", "Authentification"),
            page("v_rification_de_s_curit_asmacs", "Verification de securite", "Authentification"),
            page("test_de_niveau_supervis_par_ia_asmacs", "Test de niveau IA", "Evaluation")
    );

    @GetMapping("/interfaces")
    public String index(Model model) {
        model.addAttribute("pages", PAGES);
        return "interfaces/index";
    }

    @GetMapping("/interfaces/{slug}")
    public String show(@PathVariable String slug) {
        boolean allowed = PAGES.stream().anyMatch(page -> page.slug().equals(slug));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "interfaces/pages/" + slug;
    }

    private static InterfacePage page(String slug, String title, String category) {
        return new InterfacePage(slug, title, category, "/interfaces/screens/" + slug + ".png");
    }

    public record InterfacePage(String slug, String title, String category, String screenPath) {
    }
}
