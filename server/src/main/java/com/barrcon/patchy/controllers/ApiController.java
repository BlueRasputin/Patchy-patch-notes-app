package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.LoginFormDTO;
import com.barrcon.patchy.dto.RegisterFormDTO;
import com.barrcon.patchy.models.User;
import com.barrcon.patchy.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController

@RequestMapping("/api")
public class ApiController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public ApiController( UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final String userSessionKey = "user";

    public User getUserFromSession(HttpSession session) {
        Long userId = (Long) session.getAttribute(userSessionKey);
        if (userId == null) {
            return null;
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return null;
        }
        System.out.println("User retrieved from session: " + user.get().getUsername());
        return user.get();
    }

    private static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
    }


    public ResponseEntity<User> getCurrentUser(HttpSession session) {
        User user = getUserFromSession(session);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/currentUserId")
    public ResponseEntity<Long> getCurrentUserId (HttpSession session) {
        User user = getUserFromSession(session);
        System.out.println("Current user ID: " + (user != null ? user.getId() : "null"));
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(user.getId(),HttpStatus.OK);
    }
    //register user controller
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterFormDTO registerFormDTO, Errors errors, HttpServletRequest request) {
        if (errors.hasErrors()) {
            System.out.println(errors.getAllErrors());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User existingUser = userRepository.findByUsername(registerFormDTO.getUsername());
        // Check if username is already claimed
        if (existingUser != null) {
            return new ResponseEntity<String>("Username already exists", HttpStatus.CONFLICT);
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
        // Set user in session
        setUserInSession(request.getSession(), newUser);

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
        // Set user in session
        setUserInSession(request.getSession(), theUser);

        System.out.println("User authenticated: " + theUser.getUsername());
        System.out.println("User ID in session: " + request.getSession().getAttribute(userSessionKey));

        return new ResponseEntity<>(theUser, HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return new ResponseEntity<>("Logout successful", HttpStatus.OK);
    }


}
