package com.barrcon.patchy.services;

import com.barrcon.patchy.models.ApiToken;
import com.barrcon.patchy.models.User;
import com.barrcon.patchy.repositories.ApiTokenRepository;
import com.barrcon.patchy.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

// Single place that answers "who is calling": the browser session (website,
// browser extension) or a Bearer API token (editor plugins, MCP server).
@Service
public class CurrentUserService {

    public static final String USER_SESSION_KEY = "user";
    private static final String TOKEN_PREFIX = "pat_";

    private final UserRepository userRepository;
    private final ApiTokenRepository apiTokenRepository;
    private final SecureRandom random = new SecureRandom();

    public CurrentUserService(UserRepository userRepository, ApiTokenRepository apiTokenRepository) {
        this.userRepository = userRepository;
        this.apiTokenRepository = apiTokenRepository;
    }

    public Optional<User> resolve(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return apiTokenRepository.findByTokenHash(hash(header.substring(7).trim()))
                    .map(token -> {
                        token.setLastUsedAt(LocalDateTime.now());
                        return apiTokenRepository.save(token).getUser();
                    });
        }
        return fromSession(request.getSession(false));
    }

    // Token minting is session-only so a leaked token can't mint more tokens
    public Optional<User> fromSession(HttpSession session) {
        if (session == null || !(session.getAttribute(USER_SESSION_KEY) instanceof Long userId)) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    public void signIn(HttpSession session, User user) {
        session.setAttribute(USER_SESSION_KEY, user.getId());
    }

    // Returns the plaintext token; it is never retrievable again
    public String createToken(User user, String name) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        apiTokenRepository.save(new ApiToken(user, hash(token), name));
        return token;
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
