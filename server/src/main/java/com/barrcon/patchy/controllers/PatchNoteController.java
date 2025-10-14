package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.LivePatchNoteDTO;
import com.barrcon.patchy.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatchNoteController {

//    @GetMapping("/{userId}/the-bay")
//    public ResponseEntity<List<LivePatchNoteDTO>> getLivePatchNotesForFavoriteTechs(@PathVariable Long userId) {
//        Optional<User> optionalUser = userRepository.findById(userId);
//        if (optionalUser.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        User user = optionalUser.get();
//
//        List<LivePatchNoteDTO> livePatchNotes = user.getFavoriteTechs()
//                .parallelStream()
//                .map(tech -> geminiService.fetchLivePatchNotes(tech.getName()))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(livePatchNotes);
//    }

    @PostMapping("/api/process-crawled-notes")
    public ResponseEntity<String> processCrawledNotes(@RequestBody List<Map<String, Object>> crawledData) {
        try {
            for (Map<String, Object> item : crawledData) {
                String techName = (String) item.get("techName");
                String content = (String) item.get("content");
                String url = (String) item.get("url");
                String title = (String) item.get("title");
                String version = (String) item.get("version");

               
            }

            return ResponseEntity.ok("Processed " + crawledData.size() + " patch note sources");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Processing failed: " + e.getMessage());
        }
    }


}
