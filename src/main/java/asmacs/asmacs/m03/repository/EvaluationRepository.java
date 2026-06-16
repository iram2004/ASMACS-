package asmacs.asmacs.m03.repository;

import asmacs.asmacs.m03.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByClasseScolaire_IdAndMatiere_IdAndSequence(Long classeId, Long matiereId, Integer sequence);

    Optional<Evaluation> findByClasseScolaire_IdAndMatiere_IdAndSequenceAndType(
            Long classeId, Long matiereId, Integer sequence, String type);

    List<Evaluation> findByClasseScolaire_IdAndSequence(Long classeId, Integer sequence);
}
