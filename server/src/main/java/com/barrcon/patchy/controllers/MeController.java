package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.PatchNoteResponseDTO;
import com.barrcon.patchy.models.ApiToken;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.models.User;
import com.barrcon.patchy.repositories.ApiTokenRepository;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.repositories.UserRepository;
import com.barrcon.patchy.services.CurrentUserService;
import com.barrcon.patchy.services.PatchNoteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

// Everything about the calling user. Identity always comes from the session or
// API token, never from an id in the URL.
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final TechRepository techRepository;
    private final PatchNoteRepository patchNoteRepository;
    private final PatchNoteService patchNoteService;
    private final ApiTokenRepository apiTokenRepository;

    public MeController(CurrentUserService currentUserService, UserRepository userRepository,
                        TechRepository techRepository, PatchNoteRepository patchNoteRepository,
                        PatchNoteService patchNoteService, ApiTokenRepository apiTokenRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.techRepository = techRepository;
        this.patchNoteRepository = patchNoteRepository;
        this.patchNoteService = patchNoteService;
        this.apiTokenRepository = apiTokenRepository;
    }

    private <T> ResponseEntity<T> withUser(HttpServletRequest request, Function<User, ResponseEntity<T>> action) {
        return currentUserService.resolve(request)
                .map(action)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping
    public ResponseEntity<User> me(HttpServletRequest request) {
        return withUser(request, ResponseEntity::ok);
    }

    public record ProfileUpdate(String username) {
    }

    @PutMapping
    public ResponseEntity<?> update(HttpServletRequest request, @RequestBody ProfileUpdate update) {
        return withUser(request, user -> {
            if (update.username() != null && !update.username().isBlank() && !update.username().equals(user.getUsername())) {
                if (userRepository.existsByUsername(update.username())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
                }
                user.setUsername(update.username().trim());
            }
            return ResponseEntity.ok(userRepository.save(user));
        });
    }

    @GetMapping("/favorites")
    public ResponseEntity<Set<Tech>> favorites(HttpServletRequest request) {
        return withUser(request, user -> ResponseEntity.ok(user.getFavoriteTechs()));
    }

    public record FavoritesRequest(List<Long> techIds, List<String> techNames) {
    }

    // Bulk add, by id (website) or by name (editor sync of a project's techs)
    @PostMapping("/favorites")
    public ResponseEntity<Set<Tech>> addFavorites(HttpServletRequest request, @RequestBody FavoritesRequest body) {
        return withUser(request, user -> {
            if (body.techIds() != null) {
                body.techIds().forEach(id -> techRepository.findById(id).ifPresent(user::addFavoriteTech));
            }
            if (body.techNames() != null) {
                body.techNames().forEach(name -> techRepository.findByNameIgnoreCase(name).ifPresent(user::addFavoriteTech));
            }
            return ResponseEntity.ok(userRepository.save(user).getFavoriteTechs());
        });
    }

    @PostMapping("/favorites/{techId}")
    public ResponseEntity<Set<Tech>> addFavorite(HttpServletRequest request, @PathVariable Long techId) {
        return changeFavorite(request, techId, true);
    }

    @DeleteMapping("/favorites/{techId}")
    public ResponseEntity<Set<Tech>> removeFavorite(HttpServletRequest request, @PathVariable Long techId) {
        return changeFavorite(request, techId, false);
    }

    private ResponseEntity<Set<Tech>> changeFavorite(HttpServletRequest request, Long techId, boolean add) {
        return withUser(request, user -> {
            Optional<Tech> tech = techRepository.findById(techId);
            if (tech.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (add) {
                user.addFavoriteTech(tech.get());
            } else {
                user.removeFavoriteTech(tech.get());
            }
            return ResponseEntity.ok(userRepository.save(user).getFavoriteTechs());
        });
    }

    // Latest patch note for each of the user's favorite techs (The Bay)
    @GetMapping("/patch-notes")
    public ResponseEntity<List<PatchNoteResponseDTO>> patchNotes(HttpServletRequest request) {
        return withUser(request, user -> ResponseEntity.ok(user.getFavoriteTechs().stream()
                .map(patchNoteRepository::findFirstByTechOrderByCreatedAtDesc)
                .flatMap(Optional::stream)
                .map(patchNoteService::toResponseDTO)
                .toList()));
    }

    @GetMapping("/tokens")
    public ResponseEntity<List<ApiToken>> tokens(HttpServletRequest request) {
        return withUser(request, user -> ResponseEntity.ok(apiTokenRepository.findByUserOrderByCreatedAtDesc(user)));
    }

    public record TokenRequest(String name) {
    }

    // Session-only: the website mints tokens for editors via /connect
    @PostMapping("/tokens")
    public ResponseEntity<Map<String, String>> createToken(HttpServletRequest request, @RequestBody TokenRequest body) {
        return currentUserService.fromSession(request.getSession(false))
                .map(user -> {
                    String name = body.name() == null || body.name().isBlank() ? "Editor" : body.name().trim();
                    String clipped = name.substring(0, Math.min(name.length(), 100));
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of("name", clipped, "token", currentUserService.createToken(user, clipped)));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @DeleteMapping("/tokens/{id}")
    public ResponseEntity<Void> revokeToken(HttpServletRequest request, @PathVariable Long id) {
        return withUser(request, user -> apiTokenRepository.findById(id)
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .map(token -> {
                    apiTokenRepository.delete(token);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build()));
    }
}
