package asmacs.asmacs.m03.repository;

import asmacs.asmacs.m03.entity.CahierTexte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CahierTexteRepository extends JpaRepository<CahierTexte, Long> {

    List<CahierTexte> findByClasseScolaire_IdAndMatiere_IdOrderByDateSeanceDesc(Long classeId, Long matiereId);
}
