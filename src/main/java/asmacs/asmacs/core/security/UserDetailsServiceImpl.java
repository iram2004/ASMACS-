package asmacs.asmacs.core.security;

// ── Spring Security ────────────────────────────────────────────────────────
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;

// ── Spring ─────────────────────────────────────────────────────────────────
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ── Lombok ─────────────────────────────────────────────────────────────────
import lombok.RequiredArgsConstructor;

// ── Projet ─────────────────────────────────────────────────────────────────
import asmacs.asmacs.core.entity.Utilisateur;
import asmacs.asmacs.core.repository.UtilisateurRepository;

// ── Java ───────────────────────────────────────────────────────────────────
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // 1. Cherche l'utilisateur en base par son username
        Utilisateur user = utilisateurRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable : " + username));

        // 2. Vérifie que le compte n'est pas bloqué
        if (Boolean.FALSE.equals(user.getActif())) {
            throw new UsernameNotFoundException(
                    "Compte désactivé : " + username);
        }

        // 3. Retourne un UserDetails Spring Security
        //    avec le mot de passe hashé et le rôle ROLE_XXX
        return new User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()))
        );
    }
}