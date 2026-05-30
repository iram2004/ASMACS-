package asmacs.asmacs.m02.repository;

import asmacs.asmacs.m02.entity.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    List<Seance> findByClasseId(Long classeId);
    List<Seance> findByEnseignantId(Long enseignantId);
    List<Seance> findByStatut(String statut);
    List<Seance> findByDateSeance(LocalDate date);
    List<Seance> findByDateSeanceBetween(LocalDate debut, LocalDate fin);
    Optional<Seance> findByQrCodeToken(String token);

    List<Seance> findByClasseIdAndDateSeance(Long classeId, LocalDate date);
    List<Seance> findByEnseignantIdAndDateSeance(Long enseignantId, LocalDate date);

    @Query("SELECT s FROM Seance s WHERE s.statut = 'EN_COURS'")
    List<Seance> findEnCours();

    @Query("SELECT s FROM Seance s " +
            "WHERE s.enseignant.id = :ensId " +
            "ORDER BY s.dateSeance DESC, s.heureDebut DESC")
    List<Seance> findByEnseignantOrderByDate(@Param("ensId") Long ensId);

    long countByStatut(String statut);
}