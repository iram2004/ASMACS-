package asmacs.asmacs.m01.repository;

import asmacs.asmacs.m01.entity.DocumentCandidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository
        extends JpaRepository<DocumentCandidat, Long> {

    List<DocumentCandidat> findByCandidatureId(Long candidatureId);

    Optional<DocumentCandidat> findByCandidatureIdAndTypeDocument(
            Long candidatureId, String typeDocument);

    boolean existsByCandidatureIdAndTypeDocument(
            Long candidatureId, String typeDocument);
}