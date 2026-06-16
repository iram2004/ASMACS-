package asmacs.asmacs.m04.repository;

import asmacs.asmacs.m04.entity.SeanceCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeanceCoursRepository extends JpaRepository<SeanceCours, Long> {

    List<SeanceCours> findByClasseScolaire_IdOrderByHeureDebutAscJourAsc(Long classeId);

    boolean existsByClasseScolaire_Id(Long classeId);
}
