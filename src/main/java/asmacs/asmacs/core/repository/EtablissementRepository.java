package asmacs.asmacs.core.repository;

import asmacs.asmacs.core.entity.Etablissement;
import asmacs.asmacs.core.enums.NiveauEnseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtablissementRepository
        extends JpaRepository<Etablissement, Long> {

    Optional<Etablissement> findByCodeEtablissement(String code);
    List<Etablissement>     findByNiveauEnseignement(NiveauEnseignement niveau);
    List<Etablissement>     findByVille(String ville);
    List<Etablissement>     findByActifTrue();
    boolean                 existsByCodeEtablissement(String code);
}
