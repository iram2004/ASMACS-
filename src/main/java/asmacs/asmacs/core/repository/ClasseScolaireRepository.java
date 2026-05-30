package asmacs.asmacs.core.repository;

import asmacs.asmacs.core.entity.ClasseScolaire;
import asmacs.asmacs.core.enums.NiveauEnseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClasseScolaireRepository
        extends JpaRepository<ClasseScolaire, Long> {

    List<ClasseScolaire> findByAnneeScolaireId(Long anneeId);
    List<ClasseScolaire> findByEtablissementId(Long etablissementId);
    List<ClasseScolaire> findByNiveauEnseignement(NiveauEnseignement niveau);

    // Filtre principal utilisé par tous les modules
    List<ClasseScolaire> findByEtablissementIdAndAnneeScolaireId(
            Long etabId, Long anneeId);

    // M02 : classes affectées à un enseignant principal
    @Query("SELECT c FROM ClasseScolaire c " +
            "WHERE c.enseignantPrincipal.id = :enseignantId")
    List<ClasseScolaire> findByEnseignantPrincipalId(
            @Param("enseignantId") Long id);

    // Nombre d'élèves dans une classe
    @Query("SELECT COUNT(e) FROM Eleve e WHERE e.classeScolaire.id = :classeId")
    long countElevesInClasse(@Param("classeId") Long classeId);
}