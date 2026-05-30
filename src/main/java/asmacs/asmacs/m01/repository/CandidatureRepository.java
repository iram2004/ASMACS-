package asmacs.asmacs.m01.repository;

import asmacs.asmacs.m01.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatureRepository
        extends JpaRepository<Candidature, Long> {

    Optional<Candidature> findByNumeroCandidature(String numero);
    boolean existsByNumeroCandidature(String numero);
    boolean existsByEmail(String email);

    List<Candidature> findByStatut(String statut);
    List<Candidature> findByStatutOrderByDateSoumissionDesc(String statut);
    List<Candidature> findByEtablissementId(Long etabId);
    List<Candidature> findAllByOrderByDateSoumissionDesc();

    Optional<Candidature> findByEmail(String email);
    Optional<Candidature> findByUtilisateurCreeId(Long userId);

    @Query("SELECT c FROM Candidature c WHERE " +
            "LOWER(c.nom)    LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(c.prenom) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(c.numeroCandidature) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(c.email)  LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Candidature> search(@Param("q") String query);

    long countByStatut(String statut);

    @Query("SELECT c FROM Candidature c " +
            "ORDER BY c.scoreTest DESC NULLS LAST")
    List<Candidature> findAllOrderedByScore();
}