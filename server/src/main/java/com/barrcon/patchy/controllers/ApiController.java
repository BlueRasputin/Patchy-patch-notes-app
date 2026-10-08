package com.barrcon.patchy.controllers;

import com.barrcon.patchy.config.SecurityConfig;
import com.barrcon.patchy.dto.LoginFormDTO;
import com.barrcon.patchy.dto.RegisterFormDTO;
import com.barrcon.patchy.models.User;
import com.barrcon.patchy.repositories.UserRepository;
import com.barrcon.patchy.services.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api")
public class ApiController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final SecurityConfig.OAuthProviders oauthProviders;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ApiController(UserRepository userRepository,
                         CurrentUserService currentUserService,
                         SecurityConfig.OAuthProviders oauthProviders) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.oauthProviders = oauthProviders;
    }

    // Which "Continue with ..." buttons the login page should show
    @GetMapping("/auth/providers")
    public List<String> authProviders() {
        return oauthProviders.ids();
    }

    //register user controller
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterFormDTO registerFormDTO, Errors errors, HttpServletRequest request) {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Check if username or email is already claimed
        if (userRepository.findByUsername(registerFormDTO.getUsername()) != null) {
            return new ResponseEntity<String>("Username already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.findByEmail(registerFormDTO.getEmail()) != null) {
            return new ResponseEntity<String>("Email already in use", HttpStatus.CONFLICT);
        }
        // obtain password and verify password from DTO
        String password = registerFormDTO.getPassword();
        String verifyPassword = registerFormDTO.getVerifyPassword();
        // Check if passwords match
        if (!password.equals(verifyPassword)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Create and save new user
        User newUser = new User(registerFormDTO.getUsername(),passwordEncoder.encode(password), registerFormDTO.getEmail());
        userRepository.save(newUser);
        currentUserService.signIn(request.getSession(), newUser);

        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
    //login user controller
    @GetMapping("/login")
    public ResponseEntity<String> displayLoginForm() {
        return new ResponseEntity<>("Please log in", HttpStatus.OK);
    }
    // Process login form submission
    @PostMapping("/login")
    public ResponseEntity<User> processLoginForm(@RequestBody @Valid LoginFormDTO loginFormDTO, Errors errors, HttpServletRequest request) {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User theUser = userRepository.findByUsername(loginFormDTO.getUsername());
        // Check if user exists and if their password matches database
        if (theUser == null || !theUser.isMatchingPassword(loginFormDTO.getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // Rotate the session id on login to prevent session fixation
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        currentUserService.signIn(request.getSession(), theUser);

        return new ResponseEntity<>(theUser, HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return new ResponseEntity<>("Logout successful", HttpStatus.OK);
    }


}
