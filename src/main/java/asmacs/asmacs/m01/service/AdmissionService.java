package asmacs.asmacs.m01.service;

import asmacs.asmacs.m01.dto.CandidatureDTO;
import asmacs.asmacs.m01.dto.CandidatureFormDTO;
import asmacs.asmacs.m01.dto.PaiementDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface AdmissionService {

    // ── CANDIDAT (PUBLIC) ────────────────────────────────────
    CandidatureDTO soumettreCandidature(CandidatureFormDTO form);
    CandidatureDTO getStatutByEmail(String email);
    CandidatureDTO getById(Long id);
    CandidatureDTO getByNumero(String numero);
    CandidatureDTO enregistrerPaiement(Long candidatureId, PaiementDTO dto);
    CandidatureDTO soumettreTest(Long candidatureId, int[] reponses);

    // ── ADMIN ────────────────────────────────────────────────
    List<CandidatureDTO> getAll();
    List<CandidatureDTO> getByStatut(String statut);
    List<CandidatureDTO> search(String query);
    List<CandidatureDTO> getClassement();

    CandidatureDTO valider(Long id, Long classeId, String adminUsername);
    CandidatureDTO rejeter(Long id, String motif);
    CandidatureDTO demanderCorrection(Long id, String message);
    CandidatureDTO confirmerPaiement(Long id);

    // ── STATS ────────────────────────────────────────────────
    long countByStatut(String statut);
    long countTotal();
}