package com.barrcon.patchy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Only providers with credentials in app.env are registered, so the app
    // still boots (password login only) when OAuth isn't configured.
    public record OAuthProviders(List<ClientRegistration> registrations) {
        public List<String> ids() {
            return registrations.stream().map(ClientRegistration::getRegistrationId).toList();
        }
    }

    @Bean
    OAuthProviders oauthProviders(@Value("${GITHUB_CLIENT_ID:}") String githubId,
                                  @Value("${GITHUB_CLIENT_SECRET:}") String githubSecret,
                                  @Value("${GOOGLE_CLIENT_ID:}") String googleId,
                                  @Value("${GOOGLE_CLIENT_SECRET:}") String googleSecret) {
        List<ClientRegistration> registrations = new ArrayList<>();
        if (!githubId.isBlank()) {
            registrations.add(CommonOAuth2Provider.GITHUB.getBuilder("github")
                    .clientId(githubId).clientSecret(githubSecret).build());
        }
        if (!googleId.isBlank()) {
            registrations.add(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(googleId).clientSecret(googleSecret).build());
        }
        return new OAuthProviders(registrations);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           OAuthProviders providers,
                                           OAuthLoginSuccessHandler successHandler,
                                           @Value("${patchy.frontend-url}") String frontendUrl) throws Exception {
        http
                // CSRF off: the API only accepts JSON bodies (415 on form posts) and the
                // session cookie is SameSite=Lax. Revisit for production hardening.
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                // Per-user endpoints check the caller via CurrentUserService
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());

        if (!providers.registrations().isEmpty()) {
            http.oauth2Login(oauth -> oauth
                    .clientRegistrationRepository(new InMemoryClientRegistrationRepository(providers.registrations()))
                    .successHandler(successHandler)
                    .failureUrl(frontendUrl + "/login?error=oauth"));
        }

        return http.build();
    }
}
