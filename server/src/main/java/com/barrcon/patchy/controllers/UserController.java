package com.barrcon.patchy.controllers;
import com.barrcon.patchy.dto.TechDTO;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.models.User;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {


    private final UserRepository userRepository;
    private final TechRepository techRepository;


    @Autowired
    public UserController(UserRepository userRepository, TechRepository techRepository) {
        this.userRepository = userRepository;
        this.techRepository = techRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok((List<User>) userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    //endpoint to create user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }


    //endpoint to update user id
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(userDetails.getUsername());
            return ResponseEntity.ok(userRepository.save(user));
        }
        return ResponseEntity.notFound().build();
    }


    //delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }



    //add tech to users favorites list
    @PostMapping("/{userId}/favorites")
    public ResponseEntity<User> addFavorite(@PathVariable Long userId, @RequestBody TechDTO techDTO) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        for (Long techId : techDTO.getTechIds()) {
            Optional<Tech> optionalTech = techRepository.findById(techId);
            if (optionalTech.isPresent()) {
                user.getFavoriteTechs().add(optionalTech.get());
            }
        }

        return ResponseEntity.ok(userRepository.save(user));
    }


    @PostMapping("/{userId}/favorites/{techId}")
    public ResponseEntity<User> addFavorite(@PathVariable Long userId, @PathVariable Long techId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Tech> optionalTech = techRepository.findById(techId);

        if (optionalUser.isPresent() && optionalTech.isPresent()) {
            User user = optionalUser.get();
            user.getFavoriteTechs().add(optionalTech.get());
            return ResponseEntity.ok(userRepository.save(user));
        }

        return ResponseEntity.notFound().build();
    }


    //delete tech from users favorites
    @DeleteMapping("/{userId}/favorites/{techId}")
    public ResponseEntity<User> removeFavorite(@PathVariable Long userId, @PathVariable Long techId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Tech> optionalTech = techRepository.findById(techId);

        if (optionalUser.isPresent() && optionalTech.isPresent()) {
            User user = optionalUser.get();
            user.getFavoriteTechs().remove(optionalTech.get());
            return ResponseEntity.ok(userRepository.save(user));
        }

        return ResponseEntity.notFound().build();
    }


    //get list of users favorites
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<Set<Tech>> getUserFavorites(@PathVariable Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            return ResponseEntity.ok(optionalUser.get().getFavoriteTechs());
        }
        return ResponseEntity.notFound().build();
    }




}
