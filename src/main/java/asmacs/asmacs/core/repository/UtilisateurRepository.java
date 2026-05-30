package asmacs.asmacs.core.repository;

import asmacs.asmacs.core.entity.Utilisateur;
import asmacs.asmacs.core.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository
        extends JpaRepository<Utilisateur, Long> {

    // Appelé par UserDetailsServiceImpl à chaque connexion
    Optional<Utilisateur> findByUsername(String username);
    Optional<Utilisateur> findByEmail(String email);
    boolean               existsByUsername(String username);
    boolean               existsByEmail(String email);
    List<Utilisateur>     findByRole(Role role);
    List<Utilisateur>     findByEtablissementId(Long etablissementId);
    List<Utilisateur>     findByActifTrue();
}