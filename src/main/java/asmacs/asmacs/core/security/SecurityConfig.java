package asmacs.asmacs.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/admission",
                                "/admission/**",
                                "/login"
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()

                        .requestMatchers("/login").permitAll()

                        .requestMatchers(
                                "/admission",
                                "/admission/**"
                        ).permitAll()

                        .requestMatchers("/m12/**")
                        .hasRole("DSI")

                        .requestMatchers("/m10/**")
                        .hasAnyRole(
                                "ADMIN",
                                "DIRECTEUR",
                                "DSI",
                                "INSPECTEUR"
                        )

                        .requestMatchers("/m09/**")
                        .hasAnyRole(
                                "COMPTABLE",
                                "ADMIN",
                                "DIRECTEUR"
                        )

                        .requestMatchers("/m04/**")
                        .hasAnyRole(
                                "ADMIN",
                                "DIRECTEUR",
                                "SECRETAIRE"
                        )

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .maximumSessions(5)
                        .expiredUrl("/login?expired=true")
                );

        return http.build();
    }
}