package com.caspian.pichak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Configuration
public class CustomTokenCustomizer {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            Duration ttl = context.getRegisteredClient()
                    .getTokenSettings()
                    .getAccessTokenTimeToLive();

            Instant expiresAt = Instant.now().plus(ttl);

            context.getClaims().claim("expires", ttl.toSeconds());
            context.getClaims().claim("expires_date", Date.from(expiresAt).toString());

            Object principal = context.getPrincipal() != null
                    ? context.getPrincipal().getPrincipal()
                    : null;

            if (principal != null) {
                String principalText = principal.toString();
                String[] parts = principalText.split("\\|");

                if (parts.length > 1) {
                    context.getClaims().claim("sessionId", parts[1]);
                }
            }
        };
    }
}