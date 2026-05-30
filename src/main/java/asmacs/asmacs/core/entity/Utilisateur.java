package asmacs.asmacs.core.entity;

import asmacs.asmacs.core.enums.Role;
import asmacs.asmacs.core.enums.Sexe;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "utilisateur",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_utilisateur_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_utilisateur_email",    columnNames = "email")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    // Clé primaire — référencée en FK par Enseignant et Eleve
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Le username est obligatoire")
    @Column(name = "username", nullable = false, length = 80)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(name = "password", nullable = false)
    private String password; // Hashé BCrypt — JAMAIS en clair

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false, length = 80)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false, length = 80)
    private String prenom;

    @Email(message = "Email invalide")
    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    // Rôle Spring Security → détermine les accès aux modules
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexe")
    private Sexe sexe;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    // false = compte bloqué, ne peut pas se connecter
    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    // ── Clé étrangère → Etablissement ─────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name       = "etablissement_id",
            foreignKey = @ForeignKey(name = "fk_utilisateur_etablissement")
    )
    private Etablissement etablissement;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}