package asmacs.asmacs.core.repository;

import asmacs.asmacs.core.entity.Enseignant;
import asmacs.asmacs.core.enums.TypeEnseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnseignantRepository
        extends JpaRepository<Enseignant, Long> {

    Optional<Enseignant> findByMatricule(String matricule);
    boolean              existsByMatricule(String matricule);
    List<Enseignant>     findByEtablissementId(Long etablissementId);
    List<Enseignant>     findByTypeEnseignant(TypeEnseignant type);
    List<Enseignant>     findByActifTrue();
    List<Enseignant>     findByEtablissementIdAndActifTrue(Long id);

    // M02 : enseignants pouvant enseigner une matière donnée
    @Query("SELECT e FROM Enseignant e JOIN e.matieres m WHERE m.id = :matiereId")
    List<Enseignant> findByMatiereId(@Param("matiereId") Long matiereId);

    // Barre de recherche M04
    @Query("SELECT e FROM Enseignant e WHERE " +
            "LOWER(e.nom)    LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(e.prenom) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(e.matricule) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Enseignant> searchByNomOuPrenomOuMatricule(@Param("q") String q);
}