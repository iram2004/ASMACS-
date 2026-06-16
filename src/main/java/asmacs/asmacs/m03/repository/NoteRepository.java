package asmacs.asmacs.m03.repository;

import asmacs.asmacs.m03.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByEvaluation_Id(Long evaluationId);

    List<Note> findByEvaluation_IdIn(List<Long> evaluationIds);

    Optional<Note> findByEleve_IdAndEvaluation_Id(Long eleveId, Long evaluationId);
}
