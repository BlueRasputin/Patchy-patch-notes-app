package com.barrcon.patchy.controllers;

import com.barrcon.patchy.models.PatchNotes;
import com.barrcon.patchy.repositories.PatchNotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patch-notes")
@CrossOrigin
public class PatchNoteController {

    @Autowired
    private PatchNotesRepository patchNotesRepository;

    @GetMapping
    public ResponseEntity<List<PatchNotes>> getAllPatchNotes() {
        return ResponseEntity.ok((List<PatchNotes>) patchNotesRepository.findAll());
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

    @PutMapping("/{id}")
    public ResponseEntity<PatchNotes> updatePatchNote(@PathVariable Long id, @RequestBody PatchNotes patchNote) {
        if (!patchNotesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        patchNote.setId(id);
        return ResponseEntity.ok(patchNotesRepository.save(patchNote));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatchNote(@PathVariable Long id) {
        patchNotesRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}