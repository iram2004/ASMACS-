package asmacs.asmacs.m01.dto;

import asmacs.asmacs.core.enums.NiveauEnseignement;
import asmacs.asmacs.core.enums.Sexe;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureFormDTO {

    // ── Infos personnelles ──────────────────────────────────
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotNull(message = "Le sexe est obligatoire")
    private Sexe sexe;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date doit être dans le passé")
    private LocalDate dateNaissance;

    private String lieuNaissance;
    private String nationalite;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    private String adresse;

    // ── Infos parent ────────────────────────────────────────
    @NotBlank(message = "Le nom du parent est obligatoire")
    private String nomParent;

    @NotBlank(message = "Le téléphone du parent est obligatoire")
    private String telephoneParent;

    private String emailParent;

    // ── Infos académiques ───────────────────────────────────
    @NotNull(message = "Le niveau est obligatoire")
    private NiveauEnseignement niveauDemande;

    @NotBlank(message = "La filière est obligatoire")
    private String filiereDemandee;

    private String etablissementPrecedent;
    private String derniereClasse;
    private Double moyennePrecedente;

    // ── Documents ───────────────────────────────────────────
    private MultipartFile bulletin;
    private MultipartFile acteNaissance;
    private MultipartFile photo;
    private MultipartFile diplome;
    private MultipartFile cniParent;

    // ── Contexte ────────────────────────────────────────────
    @NotNull(message = "L'établissement est obligatoire")
    private Long etablissementId;
}