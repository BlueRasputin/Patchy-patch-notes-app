package com.barrcon.patchy.config;

import com.barrcon.patchy.models.User;
import com.barrcon.patchy.repositories.UserRepository;
import com.barrcon.patchy.services.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

// Bridges GitHub/Google login into the app's session model: find the local
// User by the provider's stable id (never by username or email, which would
// let one account claim another), or create one.
@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final String frontendUrl;

    public OAuthLoginSuccessHandler(UserRepository userRepository,
                                    CurrentUserService currentUserService,
                                    @Value("${patchy.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        boolean github = "github".equals(provider);

        String providerId = String.valueOf(oauthUser.getAttributes().get(github ? "id" : "sub"));
        User user = github ? userRepository.findByGithubId(providerId) : userRepository.findByGoogleId(providerId);

        if (user == null) {
            String email = oauthUser.getAttribute("email");
            String baseName = github ? oauthUser.getAttribute("login") : email.substring(0, email.indexOf('@'));
            if (email == null) {
                // GitHub withholds private emails; the noreply form is unique per login
                email = baseName + "@users.noreply.github.com";
            }
            if (userRepository.findByEmail(email) != null) {
                // ponytail: no account linking yet; add a "connect GitHub/Google" flow on the profile page
                response.sendRedirect(frontendUrl + "/login?error=email-in-use");
                return;
            }

            user = new User(uniqueUsername(baseName), new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()), email);
            if (github) {
                user.setGithubId(providerId);
            } else {
                user.setGoogleId(providerId);
            }
            userRepository.save(user);
        }

        currentUserService.signIn(request.getSession(), user);
        response.sendRedirect(frontendUrl + "/TheBay");
    }

    private String uniqueUsername(String base) {
        String candidate = base;
        for (int suffix = 2; userRepository.existsByUsername(candidate); suffix++) {
            candidate = base + "-" + suffix;
        }
        return candidate;
    }
}
