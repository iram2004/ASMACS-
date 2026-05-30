package asmacs.asmacs.core.repository;

import asmacs.asmacs.core.entity.Eleve;
import asmacs.asmacs.core.enums.StatutEleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EleveRepository
        extends JpaRepository<Eleve, Long> {

    Optional<Eleve> findByMatricule(String matricule);
    boolean         existsByMatricule(String matricule);

    // M01 : listes par classe / établissement / année
    List<Eleve> findByClasseScolaireId(Long classeId);
    List<Eleve> findByEtablissementId(Long etablissementId);
    List<Eleve> findByAnneeScolaireId(Long anneeId);
    List<Eleve> findByStatutEleve(StatutEleve statut);

    // Filtre principal multi-critères
    List<Eleve> findByEtablissementIdAndAnneeScolaireId(
            Long etablissementId, Long anneeId);

    List<Eleve> findByClasseScolaireIdAndAnneeScolaireId(
            Long classeId, Long anneeId);

    // Recherche fulltext — barre de recherche M01
    @Query("SELECT e FROM Eleve e WHERE " +
            "LOWER(e.nom)       LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(e.prenom)    LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(e.matricule) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Eleve> searchByNomOuPrenomOuMatricule(@Param("q") String query);

    // M10 : comptages pour statistiques
    @Query("SELECT COUNT(e) FROM Eleve e WHERE e.etablissement.id = :id")
    long countByEtablissementId(@Param("id") Long id);

    @Query("SELECT COUNT(e) FROM Eleve e " +
            "WHERE e.etablissement.id = :id " +
            "AND   e.anneeScolaire.id = :anneeId")
    long countByEtablissementIdAndAnneeScolaireId(
            @Param("id") Long id, @Param("anneeId") Long anneeId);
}