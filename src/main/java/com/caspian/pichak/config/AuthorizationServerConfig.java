package com.caspian.pichak.config;

import com.caspian.pichak.model.entity.OauthClientDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(endpointsMatcher)
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher));

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("https://localhost:8082")
                .tokenEndpoint("/oauth2/token")
                .build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(com.caspian.pichak.repository.OauthClientDetailsDao dao) {
        List<RegisteredClient> registeredClients = new ArrayList<>();

        for (OauthClientDetails client : dao.findAll()) {
            if (Boolean.FALSE.equals(client.getEnabled())) {
                continue;
            }

            RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(client.getClientId())
                    .clientSecret(normalizeClientSecret(client.getClientSecret()))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(seconds(client.getAccessTokenValidity(), 3600))
                            .refreshTokenTimeToLive(seconds(client.getRefreshTokenValidity(), 86400))
                            .build());

            addScopes(builder, client.getScope());
            addGrantTypes(builder, client.getAuthorizedGrantTypes(), client.getWebServerRedirectUri());

            registeredClients.add(builder.build());
        }

        return new InMemoryRegisteredClientRepository(registeredClients);
    }

    private void addScopes(RegisteredClient.Builder builder, String scopes) {
        for (String scope : splitCsv(scopes)) {
            builder.scope(scope);
        }
    }

    private void addGrantTypes(
            RegisteredClient.Builder builder,
            String grantTypes,
            String redirectUri
    ) {
        List<String> grants = splitCsv(grantTypes);

        if (grants.isEmpty()) {
            builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
            return;
        }

        for (String grant : grants) {
            switch (grant) {
                case "client_credentials" ->
                        builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);

                case "refresh_token" ->
                        builder.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);

                case "authorization_code" -> {
                    if (redirectUri != null && !redirectUri.isBlank()) {
                        builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
                        builder.redirectUri(redirectUri.trim());
                    }
                    // If no redirectUri exists, skip authorization_code for now.
                }

                default -> {
                    // password grant is old Spring OAuth style.
                    // We will add it later if needed.
                }
            }
        }
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private Duration seconds(Integer value, int defaultSeconds) {
        int seconds = value != null && value > 0 ? value : defaultSeconds;
        return Duration.ofSeconds(seconds);
    }

    private String normalizeClientSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return "{noop}";
        }

        if (secret.startsWith("{noop}") ||
                secret.startsWith("{bcrypt}") ||
                secret.startsWith("{pbkdf2}") ||
                secret.startsWith("{scrypt}") ||
                secret.startsWith("{argon2}")) {
            return secret;
        }

        if (secret.startsWith("$2a$") || secret.startsWith("$2b$") || secret.startsWith("$2y$")) {
            return "{bcrypt}" + secret;
        }

        return "{noop}" + secret;
    }
}