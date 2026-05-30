package asmacs.asmacs.m02.repository;

import asmacs.asmacs.m02.entity.AlerteFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlerteFraudeRepository extends JpaRepository<AlerteFraude, Long> {

    List<AlerteFraude> findByStatut(String statut);
    List<AlerteFraude> findByEleveId(Long eleveId);
    List<AlerteFraude> findByTypeAlerte(String type);
    List<AlerteFraude> findAllByOrderByDateAlerteDesc();
    long countByStatut(String statut);
}