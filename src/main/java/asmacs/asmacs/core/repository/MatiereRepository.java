package asmacs.asmacs.core.repository;

import asmacs.asmacs.core.entity.Matiere;
import asmacs.asmacs.core.enums.NiveauEnseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatiereRepository
        extends JpaRepository<Matiere, Long> {

    Optional<Matiere> findByCode(String code);
    boolean           existsByCode(String code);
    List<Matiere>     findByNiveauEnseignement(NiveauEnseignement niveau);
    List<Matiere>     findByFiliere(String filiere);
    List<Matiere>     findByObligatoireTrue();
    List<Matiere>     findByNiveauEnseignementAndFiliere(
            NiveauEnseignement niveau, String filiere);
}