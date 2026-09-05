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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

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
                        .requestMatchers(HttpMethod.POST, "/api/users/registrarUser", "/api/users/login", "/api/users/reenviar-verificacao", "/api/users/confirmar-exclusao", "/api/users/confirmar-troca-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/csrf", "/api/users/verificar-email").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Trava explícita, além do management.endpoints.web.exposure.include=health
                        // do application.properties: se algum dia esse "include" for ampliado
                        // (env, beans, configprops...) por engano, esses endpoints continuam
                        // exigindo login em vez de cair no anyRequest().permitAll() do fim.
                        .requestMatchers("/actuator/**").authenticated()
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
                        .ignoringRequestMatchers("/api/users/registrarUser", "/api/users/login", "/api/users/confirmar-exclusao", "/api/users/confirmar-troca-email", "/ws/**")
                )
                .headers(headers -> headers
                        // Restringe de onde o navegador pode carregar/executar recursos.
                        // script-src só 'self' (nada de inline nem CDN externo); os únicos
                        // domínios de fora liberados são os do Google Fonts, usados pela UI.
                        // 'unsafe-inline' em style-src é necessário pro atributo style=""
                        // que o React gera para cores dinâmicas — restringir isso exigiria
                        // trocar todo estilo inline por classes CSS, sem ganho real de
                        // segurança (o risco de verdade, injeção de script, já está coberto).
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self'; " +
                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                "font-src 'self' https://fonts.gstatic.com; " +
                                "img-src 'self' data:; " +
                                "connect-src 'self'; " +
                                "object-src 'none'; " +
                                "base-uri 'self'; " +
                                "frame-ancestors 'none'"
                        ))
                        // Força HTTPS em requisições futuras, inclusive subdomínios, por 1
                        // ano. Só é enviado quando a requisição já chegou em HTTPS (o Render
                        // termina o TLS e repassa isso via X-Forwarded-Proto).
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                );

        return http.build();
    }
}
