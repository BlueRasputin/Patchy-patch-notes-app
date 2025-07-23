package com.barrcon.patchy.controllers;

import com.barrcon.patchy.models.Favorites;
import com.barrcon.patchy.models.Feed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import com.barrcon.patchy.repositories.FavoritesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/favorites")
@CrossOrigin
public class FavoritesController {

    @Autowired
    private FavoritesRepository favoritesRepository;

    @GetMapping
    public ResponseEntity<List<Favorites>> getAllFavorites() {
        return ResponseEntity.ok((List<Favorites>) favoritesRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Favorites> getFavoriteById(@PathVariable Long id) {
        Optional<Favorites> favorite = favoritesRepository.findById(id);
        return favorite.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Favorites> createFavorite(@RequestBody Favorites favorite) {
        return ResponseEntity.ok(favoritesRepository.save(favorite));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long id) {
        favoritesRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

