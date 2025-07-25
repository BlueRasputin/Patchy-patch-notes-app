package com.barrcon.patchy.controllers;
import com.barrcon.patchy.dto.PatchNotesProcessingRequest;
import com.barrcon.patchy.dto.ProcessedPatchNotesDTO;
import com.barrcon.patchy.services.GeminiPatchNotesService;
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

    @Autowired
    private GeminiPatchNotesService aiService;

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

    @PostMapping("/process")
    public ResponseEntity<ProcessedPatchNotesDTO> processPatchNotes(@RequestBody PatchNotesProcessingRequest request) {
        try {
        ProcessedPatchNotesDTO processed = aiService.processPatchNotes(

                request.getVersion(),
                request.getTitle()
        );
        PatchNotes patchNote = new PatchNotes(processed.getDescription(), processed.getVersion(), processed.getTitle());
        patchNotesRepository.save(patchNote);
        return ResponseEntity.ok(processed);
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<ProcessedPatchNotesDTO> processPatchNoteById(@PathVariable Long id) {
        Optional<PatchNotes> patchNote = patchNotesRepository.findById(id);
        if (patchNote.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PatchNotes note = patchNote.get();
        try {
            ProcessedPatchNotesDTO processed = aiService.processPatchNotes(
                    note.getVersion(),
                    note.getTitle()
            );
            return ResponseEntity.ok(processed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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