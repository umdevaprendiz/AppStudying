package com.example.AppStudying.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // BREACH protection: não fixa o token num atributo de request server-side rendered,
        // deixa o CsrfFilter resolver via cookie a cada requisição (padrão recomendado p/ SPA).
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users/registrarUser", "/api/users/login", "/api/users/reenviar-verificacao").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/csrf", "/api/users/verificar-email").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Só a API e o WebSocket exigem autenticação. O resto (o shell do
                        // React embutido no jar: index.html, /assets/**, rotas do React
                        // Router) precisa carregar pra qualquer um — inclusive a própria
                        // tela de login — sem gate de sessão na frente.
                        .requestMatchers("/api/**", "/ws/**").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf
                        // Cookie legível por JS (XSRF-TOKEN) + header (X-XSRF-TOKEN) é o padrão
                        // pra proteger uma API REST consumida por SPA sem usar formulários HTML.
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        // Login/registro não têm sessão autenticada ainda para proteger, e o
                        // handshake do SockJS não consegue enviar o header CSRF.
                        .ignoringRequestMatchers("/api/users/registrarUser", "/api/users/login", "/ws/**")
                );

        return http.build();
    }
}
