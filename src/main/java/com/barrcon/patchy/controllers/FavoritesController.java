package com.barrcon.patchy.controllers;


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
    public ResponseEntity<PatchNotes> getPatchNoteById(@PathVariable Long id) {
        Optional<PatchNotes> patchNote = patchNotesRepository.findById(id);
        return patchNote.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PatchNotes> createPatchNote(@RequestBody PatchNotes patchNote) {
        return ResponseEntity.ok(patchNotesRepository.save(patchNote));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatchNote(@PathVariable Long id) {
        patchNotesRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
