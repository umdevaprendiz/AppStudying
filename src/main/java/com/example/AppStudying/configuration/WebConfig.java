package com.example.AppStudying.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    // Em produção, front e back são servidos pela mesma origem (o build do React
    // é embutido no jar), então não é preciso CORS nenhum: essa propriedade fica
    // vazia por padrão. Só preencha (via variável de ambiente, lista separada por
    // vírgula) se um domínio de frontend separado precisar chamar a API.
    @Value("${app.cors.allowed-origins:}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (allowedOrigins.length == 0 || allowedOrigins[0].isBlank()) {
                    return;
                }
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
