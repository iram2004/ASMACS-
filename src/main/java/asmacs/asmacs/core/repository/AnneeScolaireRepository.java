package asmacs.asmacs.core.repository;

import asmacs.asmacs.core.entity.AnneeScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnneeScolaireRepository
        extends JpaRepository<AnneeScolaire, Long> {

    Optional<AnneeScolaire> findByLibelleAndEtablissementId(
            String libelle, Long etablissementId);

    Optional<AnneeScolaire> findByEnCoursTrueAndEtablissementId(
            Long etablissementId);

    // Retourne l'année active globale — utilisée dans les modules
    Optional<AnneeScolaire> findByEnCoursTrue();

    List<AnneeScolaire> findByEtablissementId(Long etablissementId);
    List<AnneeScolaire> findByEtablissementIdOrderByLibelleDesc(Long id);
}