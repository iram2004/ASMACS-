package asmacs.asmacs.m02.repository;

import asmacs.asmacs.m02.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    List<Presence> findBySeanceId(Long seanceId);
    List<Presence> findByEleveId(Long eleveId);
    List<Presence> findByStatut(String statut);
    Optional<Presence> findByEleveIdAndSeanceId(Long eleveId, Long seanceId);
    boolean existsByEleveIdAndSeanceId(Long eleveId, Long seanceId);

    List<Presence> findByEleveIdAndStatut(Long eleveId, String statut);
    List<Presence> findByFraudeDetecteeTrue();

    @Query("SELECT p FROM Presence p " +
            "WHERE p.seance.classe.id = :classeId " +
            "AND p.statut = :statut")
    List<Presence> findByClasseAndStatut(
            @Param("classeId") Long classeId,
            @Param("statut") String statut);

    @Query("SELECT p FROM Presence p " +
            "WHERE p.seance.dateSeance BETWEEN :debut AND :fin")
    List<Presence> findByDateBetween(
            @Param("debut") LocalDate debut,
            @Param("fin")   LocalDate fin);

    @Query("SELECT p FROM Presence p " +
            "WHERE p.eleve.id = :eleveId " +
            "ORDER BY p.seance.dateSeance DESC")
    List<Presence> findByEleveOrderByDate(@Param("eleveId") Long eleveId);

    // Stats
    long countBySeanceId(Long seanceId);
    long countBySeanceIdAndStatut(Long seanceId, String statut);

    @Query("SELECT COUNT(p) FROM Presence p " +
            "WHERE p.eleve.id = :eleveId AND p.statut = :statut")
    long countByEleveAndStatut(
            @Param("eleveId") Long eleveId,
            @Param("statut")  String statut);

    @Query("SELECT p.eleve.id, COUNT(p) FROM Presence p " +
            "WHERE p.statut = 'ABSENT' AND p.justifiee = false " +
            "GROUP BY p.eleve.id ORDER BY COUNT(p) DESC")
    List<Object[]> findAbsentsFrequents();
}