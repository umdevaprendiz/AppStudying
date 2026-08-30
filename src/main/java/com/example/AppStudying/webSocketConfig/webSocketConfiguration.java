package com.example.AppStudying.webSocketConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class webSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    // Em dev, o proxy do Vite preserva o header Origin original (localhost:5173)
    // ao encaminhar pro backend em :8080, então precisamos liberar essa origem
    // explicitamente (ver application.properties). Em produção (mesma origem,
    // front embutido no jar) deixe essa propriedade vazia: sem chamar
    // setAllowedOriginPatterns, o Spring já restringe à própria origem por padrão.
    @Value("${app.cors.allowed-origins:}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        var endpoint = registry.addEndpoint("/ws");
        if (allowedOrigins.length > 0 && !allowedOrigins[0].isBlank()) {
            endpoint.setAllowedOriginPatterns(allowedOrigins);
        }
        endpoint.withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
