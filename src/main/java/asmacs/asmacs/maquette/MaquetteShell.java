package asmacs.asmacs.maquette;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

/**
 * Construit le « shell » de design ASMACS (barre de rôle + sidebar + en-tête)
 * et l'injecte dans le modèle. Partagé par la maquette ({@link MaquetteController})
 * et par les modules réels (m01..m12) afin qu'ils réutilisent exactement la même
 * coque visuelle et la même navigation.
 *
 * Chaque item de nav porte une {@code url} : pointée vers l'écran réel du module
 * quand il existe (ex. {@code /m03/...}), sinon vers la coque {@code /maquette/...}.
 */
@Component
public class MaquetteShell {

    // ------------------------------------------------------------------ icônes SVG
    private static final Map<String, String[]> ICONS = Map.ofEntries(
            Map.entry("dashboard", new String[]{"M4 4h7v7H4z", "M13 4h7v4h-7z", "M13 11h7v9h-7z", "M4 13h7v7H4z"}),
            Map.entry("chart", new String[]{"M4 19V5", "M4 19h16", "M8 16v-3", "M12 16V9", "M16 16v-6"}),
            Map.entry("users", new String[]{"M9 11a3.4 3.4 0 1 0 0-6.8A3.4 3.4 0 0 0 9 11Z", "M3 20a6 6 0 0 1 12 0", "M16 4.6a3 3 0 0 1 0 5.8", "M18 14.2a5.5 5.5 0 0 1 3 5"}),
            Map.entry("calendar", new String[]{"M4 6.5A1.5 1.5 0 0 1 5.5 5h13A1.5 1.5 0 0 1 20 6.5V19a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1z", "M4 9.2h16", "M8 3.2v3.6", "M16 3.2v3.6"}),
            Map.entry("shield", new String[]{"M12 3l7 3v5c0 4.5-3 7.6-7 9-4-1.4-7-4.5-7-9V6z", "M9 12l2 2 4-4"}),
            Map.entry("clipboard", new String[]{"M9 4.5h6v2.6H9z", "M15 5.5h3.5V20H5.5V5.5H9", "M8.5 11.5h7", "M8.5 15h7"}),
            Map.entry("wallet", new String[]{"M3 7.5A1.5 1.5 0 0 1 4.5 6H17a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z", "M16 12.2h3.5", "M3 9.5h12"}),
            Map.entry("briefcase", new String[]{"M4 8.5h16V19H4z", "M9 8.5V5.8A1.3 1.3 0 0 1 10.3 4.5h3.4A1.3 1.3 0 0 1 15 5.8V8.5", "M4 13h16"}),
            Map.entry("book", new String[]{"M5 4.5h10.5a1.5 1.5 0 0 1 1.5 1.5V19.5H6.5A1.5 1.5 0 0 1 5 18z", "M5 18a1.5 1.5 0 0 1 1.5-1.5H17"}),
            Map.entry("bus", new String[]{"M5.5 5h13v9.5h-13z", "M5.5 11h13", "M7.5 18.5v-2", "M16.5 18.5v-2", "M5.5 14.5a1 1 0 0 0 1 1h11a1 1 0 0 0 1-1", "M8 5v6"}),
            Map.entry("chat", new String[]{"M4 5.5h16v9.5H9l-4 3.5z"}),
            Map.entry("inbox", new String[]{"M4 13l2.2-7.5h11.6L20 13", "M4 13v6h16v-6", "M4 13h4.5l1 2h5l1-2H20"}),
            Map.entry("play", new String[]{"M7 5l11 7-11 7z"}),
            Map.entry("gear", new String[]{"M12 9.2a2.8 2.8 0 1 0 0 5.6 2.8 2.8 0 0 0 0-5.6Z", "M19.3 12a7 7 0 0 0-.12-1.2l1.9-1.4-1.8-3.1-2.2 1a7 7 0 0 0-2-1.2l-.34-2.4H10.9l-.34 2.4a7 7 0 0 0-2 1.2l-2.2-1-1.8 3.1 1.9 1.4a7 7 0 0 0 0 2.4l-1.9 1.4 1.8 3.1 2.2-1a7 7 0 0 0 2 1.2l.34 2.4h2.2l.34-2.4a7 7 0 0 0 2-1.2l2.2 1 1.8-3.1-1.9-1.4A7 7 0 0 0 19.3 12Z"}),
            Map.entry("home", new String[]{"M4 11.2 12 4.5l8 6.7", "M6.2 9.7V19.5h11.6V9.7"}),
            Map.entry("cap", new String[]{"M3 9.2 12 5.2l9 4-9 4z", "M7 11.2v4.4c0 1 2.2 2 5 2s5-1 5-2v-4.4"}),
            Map.entry("scan", new String[]{"M5 9V6.5A1.5 1.5 0 0 1 6.5 5H9", "M15 5h2.5A1.5 1.5 0 0 1 19 6.5V9", "M19 15v2.5a1.5 1.5 0 0 1-1.5 1.5H15", "M9 19H6.5A1.5 1.5 0 0 1 5 17.5V15", "M4 12h16"})
    );

    private static String icon(String name) {
        String[] paths = ICONS.getOrDefault(name, ICONS.get("dashboard"));
        StringBuilder sb = new StringBuilder(
                "<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" "
                + "stroke-width=\"1.7\" stroke-linecap=\"round\" stroke-linejoin=\"round\">");
        for (String d : paths) sb.append("<path d=\"").append(d).append("\"></path>");
        return sb.append("</svg>").toString();
    }

    /** key = identifiant d'écran (highlight), url = lien réel, badge facultatif. */
    private static Map<String, Object> item(String key, String label, String iconName, String badge, String url) {
        return Map.of("key", key, "label", label, "icon", icon(iconName),
                "badge", badge == null ? "" : badge,
                "url", url != null ? url : ("/maquette/" + key));
    }

    private static Map<String, Object> group(String label, List<Map<String, Object>> items) {
        return Map.of("label", label, "items", items);
    }

    // ------------------------------------------------------------------ navigation par portail
    public List<Map<String, Object>> nav(String role) {
        return switch (role) {
            case "admin" -> List.of(
                    group("Pilotage", List.of(
                            item("admin/dashboard", "Tableau de bord", "dashboard", null, null),
                            item("admin/stats", "Statistiques & rapports", "chart", null, null))),
                    group("Scolarité", List.of(
                            item("admin/students", "Élèves & inscriptions", "users", null, null),
                            item("admin/timetable", "Classes & emplois du temps", "calendar", null, "/m04/admin/edt"),
                            item("admin/admission", "Admission en ligne", "inbox", null, null))),
                    group("Vie scolaire", List.of(
                            item("admin/attendance", "Présence 4.0", "shield", "14", null),
                            item("admin/grades", "Examens & notes", "clipboard", null, "/m03/admin/notes"),
                            item("admin/transport", "Transport scolaire", "bus", null, null))),
                    group("Ressources", List.of(
                            item("admin/library", "Bibliothèque", "book", null, null),
                            item("admin/elearning", "E-learning", "play", null, null))),
                    group("Gestion", List.of(
                            item("admin/finance", "Finance & paiements", "wallet", null, null),
                            item("admin/hr", "RH & paie", "briefcase", null, null))),
                    group("Échanges", List.of(
                            item("admin/comm", "Messagerie & alertes", "chat", null, null))),
                    group("Système", List.of(
                            item("admin/settings", "Paramètres", "gear", null, null))));
            case "teacher" -> List.of(
                    group("Mon espace", List.of(
                            item("teacher/dashboard", "Tableau de bord", "dashboard", null, null),
                            item("teacher/classes", "Mes classes", "users", null, null))),
                    group("Pédagogie", List.of(
                            item("teacher/grades", "Saisie des notes", "clipboard", null, "/m03/prof/notes"),
                            item("teacher/attendance", "Présence (scan)", "scan", null, null),
                            item("teacher/logbook", "Cahier de texte", "book", null, null))),
                    group("Échanges", List.of(
                            item("teacher/messages", "Messagerie", "chat", null, null))));
            case "parent" -> List.of(
                    group("Suivi", List.of(
                            item("parent/dashboard", "Accueil", "home", null, null),
                            item("parent/child", "Scolarité de l’enfant", "cap", null, null))),
                    group("Services", List.of(
                            item("parent/payments", "Paiements", "wallet", null, null),
                            item("parent/messages", "Messagerie & alertes", "chat", "2", null))));
            case "student" -> List.of(
                    group("Mon espace", List.of(
                            item("student/dashboard", "Accueil", "home", null, null),
                            item("student/grades", "Mes notes & bulletin", "clipboard", null, "/m03/eleve/bulletin"),
                            item("student/timetable", "Emploi du temps", "calendar", null, "/m04/eleve/edt"))),
                    group("Apprendre", List.of(
                            item("student/elearning", "E-learning", "play", null, null),
                            item("student/library", "Bibliothèque", "book", null, null))));
            default -> List.of();
        };
    }

    public Map<String, String> meta(String role) {
        return switch (role) {
            case "teacher" -> Map.of("roleLabel", "Espace enseignant", "schoolName", "Mme Nkolo Aline",
                    "schoolSub", "Maths · 5 classes", "schoolInit", "NA",
                    "userName", "Mme Nkolo Aline", "userRole", "Enseignante · Maths", "userInit", "NA");
            case "parent" -> Map.of("roleLabel", "Espace parent", "schoolName", "Famille Talla",
                    "schoolSub", "2 enfants suivis", "schoolInit", "TA",
                    "userName", "M. Talla Pascal", "userRole", "Parent", "userInit", "TP");
            case "student" -> Map.of("roleLabel", "Espace élève", "schoolName", "Nanda Arnold",
                    "schoolSub", "Classe de 3ᵉ A", "schoolInit", "NA",
                    "userName", "Nanda Arnold", "userRole", "Élève · 3ᵉ A", "userInit", "NA");
            default -> Map.of("roleLabel", "Administration", "schoolName", "CB La Conformité",
                    "schoolSub", "Douala · 1 248 élèves", "schoolInit", "LC",
                    "userName", "M. Etoa Bernard", "userRole", "Proviseur", "userInit", "EB");
        };
    }

    private static final Map<String, String[]> TITLES = Map.ofEntries(
            Map.entry("public/home", new String[]{"Public", "Accueil"}),
            Map.entry("public/login", new String[]{"Public", "Connexion"}),
            Map.entry("public/signup", new String[]{"Public", "Inscription"}),
            Map.entry("admin/dashboard", new String[]{"Pilotage", "Tableau de bord"}),
            Map.entry("admin/stats", new String[]{"Pilotage", "Statistiques & rapports"}),
            Map.entry("admin/students", new String[]{"Scolarité", "Élèves & inscriptions"}),
            Map.entry("admin/student", new String[]{"Scolarité", "Fiche élève"}),
            Map.entry("admin/timetable", new String[]{"Scolarité", "Classes & emplois du temps"}),
            Map.entry("admin/admission", new String[]{"Scolarité", "Admission en ligne"}),
            Map.entry("admin/attendance", new String[]{"Vie scolaire", "Présence 4.0 · Anti-fraude"}),
            Map.entry("admin/grades", new String[]{"Vie scolaire", "Examens & notes"}),
            Map.entry("admin/transport", new String[]{"Vie scolaire", "Transport scolaire"}),
            Map.entry("admin/library", new String[]{"Ressources", "Bibliothèque numérique"}),
            Map.entry("admin/elearning", new String[]{"Ressources", "E-learning"}),
            Map.entry("admin/finance", new String[]{"Gestion", "Finance & paiements"}),
            Map.entry("admin/hr", new String[]{"Gestion", "RH & paie"}),
            Map.entry("admin/comm", new String[]{"Échanges", "Messagerie & alertes"}),
            Map.entry("admin/settings", new String[]{"Système", "Paramètres"}),
            Map.entry("teacher/dashboard", new String[]{"Espace enseignant", "Tableau de bord"}),
            Map.entry("teacher/classes", new String[]{"Espace enseignant", "Mes classes"}),
            Map.entry("teacher/grades", new String[]{"Pédagogie", "Saisie des notes"}),
            Map.entry("teacher/attendance", new String[]{"Pédagogie", "Présence · Scan"}),
            Map.entry("teacher/logbook", new String[]{"Pédagogie", "Cahier de texte"}),
            Map.entry("teacher/messages", new String[]{"Échanges", "Messagerie"}),
            Map.entry("parent/dashboard", new String[]{"Espace parent", "Accueil"}),
            Map.entry("parent/child", new String[]{"Suivi", "Scolarité de l’enfant"}),
            Map.entry("parent/payments", new String[]{"Services", "Paiements"}),
            Map.entry("parent/messages", new String[]{"Services", "Messagerie & alertes"}),
            Map.entry("student/dashboard", new String[]{"Espace élève", "Accueil"}),
            Map.entry("student/grades", new String[]{"Mon espace", "Mes notes & bulletin"}),
            Map.entry("student/timetable", new String[]{"Mon espace", "Emploi du temps"}),
            Map.entry("student/elearning", new String[]{"Apprendre", "E-learning"}),
            Map.entry("student/library", new String[]{"Apprendre", "Bibliothèque"})
    );

    public List<Map<String, Object>> roleTabs() {
        return List.of(
                Map.of("key", "public", "label", "Site public", "href", "/maquette/public/home"),
                Map.of("key", "admin", "label", "Administration", "href", "/maquette/admin/dashboard"),
                Map.of("key", "teacher", "label", "Enseignant", "href", "/maquette/teacher/dashboard"),
                Map.of("key", "parent", "label", "Parent", "href", "/maquette/parent/dashboard"),
                Map.of("key", "student", "label", "Élève", "href", "/maquette/student/dashboard"));
    }

    public boolean isKnown(String active) {
        return TITLES.containsKey(active);
    }

    /**
     * Renseigne le modèle pour le fragment {@code maquette/layout :: page}.
     * @param active identifiant d'écran (ex. "teacher/grades") pour le surlignage de nav.
     */
    public void apply(Model model, String role, String active) {
        String[] tt = TITLES.getOrDefault(active, new String[]{"", ""});
        model.addAttribute("roleTabs", roleTabs());
        model.addAttribute("role", role);
        model.addAttribute("active", active);
        model.addAttribute("crumbSection", tt[0]);
        model.addAttribute("crumbTitle", tt[1]);
        if (!"public".equals(role)) {
            model.addAttribute("navGroups", nav(role));
            model.addAllAttributes(meta(role));
        }
    }
}
