//package com.barrcon.patchy.controllers;
//
//import com.barrcon.patchy.dto.LivePatchNoteDTO;
//import com.barrcon.patchy.models.User;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class PatchNoteController {
//
////    @GetMapping("/{userId}/the-bay")
////    public ResponseEntity<List<LivePatchNoteDTO>> getLivePatchNotesForFavoriteTechs(@PathVariable Long userId) {
////        Optional<User> optionalUser = userRepository.findById(userId);
////        if (optionalUser.isEmpty()) {
////            return ResponseEntity.notFound().build();
////        }
////
////        User user = optionalUser.get();
////
////        List<LivePatchNoteDTO> livePatchNotes = user.getFavoriteTechs()
////                .parallelStream()
////                .map(tech -> geminiService.fetchLivePatchNotes(tech.getName()))
////                .collect(Collectors.toList());
////
////        return ResponseEntity.ok(livePatchNotes);
////    }
//
//    @PostMapping("/api/process-crawled-notes")
//    public ResponseEntity<String> processCrawledNotes(@RequestBody List<Map<String, Object>> crawledData) {
//        try {
//            for (Map<String, Object> item : crawledData) {
//                String techName = (String) item.get("techName");
//                String content = (String) item.get("content");
//                String url = (String) item.get("url");
//                String title = (String) item.get("title");
//                String version = (String) item.get("version");
//
//
//            }
//
//            return ResponseEntity.ok("Processed " + crawledData.size() + " patch note sources");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Processing failed: " + e.getMessage());
//        }
//    }
//
//
//}

//package com.barrcon.patchy.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@CrossOrigin(origins = "http://localhost:5173")
//public class PatchNoteController {
//
//    private static final Logger log = LoggerFactory.getLogger(PatchNoteController.class);
//
//    @PostMapping("/api/process-crawled-notes")
//    public ResponseEntity<String> processCrawledNotes(@RequestBody List<Map<String, Object>> crawledData) {
//        log.info("📥 Received crawled data for {} items", crawledData.size());
//
//        try {
//            for (Map<String, Object> item : crawledData) {
//                String techName = (String) item.get("techName");
//                String version = (String) item.get("version");
//                String content = (String) item.get("content");
//                String url = (String) item.get("url");
//
//                log.info("🔄 Processing: {} v{} (Content: {} chars)", techName, version, content.length());
//
//                // For now, just log the data - you can add AI processing later
//                log.info("📄 Content preview: {}", content.substring(0, Math.min(200, content.length())));
//            }
//
//            String message = "Successfully processed " + crawledData.size() + " patch note sources";
//            log.info("🎉 " + message);
//            return ResponseEntity.ok(message);
//
//        } catch (Exception e) {
//            log.error("💥 Processing failed: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest().body("Processing failed: " + e.getMessage());
//        }
//    }
//}


//package com.barrcon.patchy.controllers;
//
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////
////import java.util.List;
////import java.util.Map;
////
////@RestController
////@CrossOrigin(origins = "http://localhost:5173")
////public class PatchNoteController {
////
////    private static final Logger log = LoggerFactory.getLogger(PatchNoteController.class);
////
////    @PostMapping("/api/process-crawled-notes")
////    public ResponseEntity<String> processCrawledNotes(@RequestBody List<Map<String, Object>> crawledData) {
////        log.info("Received crawled data for {} items", crawledData.size());
////
////        for (Map<String, Object> item : crawledData) {
////            String techName = (String) item.get("techName");
////            String version = (String) item.get("version");
////            String content = (String) item.get("content");
////            String url = (String) item.get("url");
////
////            log.info("Processing: {} v{}", techName, version);
////        }
////
////        return ResponseEntity.ok("Processed " + crawledData.size() + " patch notes");
////    }
////}





package com.barrcon.patchy.controllers;

import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class PatchNoteController {

    private static final Logger log = LoggerFactory.getLogger(PatchNoteController.class);

    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Autowired
    private TechRepository techRepository;

    @PostMapping("/api/process-crawled-notes")
    public ResponseEntity<String> processCrawledNotes(@RequestBody List<Map<String, Object>> crawledData) {
        log.info("📥 Received crawled data for {} items", crawledData.size());

        int savedCount = 0;
        int skippedCount = 0;

        try {
            for (Map<String, Object> item : crawledData) {
                String techName = (String) item.get("techName");
                String version = (String) item.get("version");
                String content = (String) item.get("content");
                String url = (String) item.get("url");

                log.info("🔄 Processing: {} v{} (Content: {} chars)",
                        techName, version, content != null ? content.length() : 0);

                // Find tech in database
                Optional<Tech> techOptional = techRepository.findByName(techName);
                if (techOptional.isEmpty()) {
                    log.warn("⚠️ Tech '{}' not found in database", techName);
                    skippedCount++;
                    continue;
                }

                Tech tech = techOptional.get();

                // Check if this version already exists
                Optional<PatchNote> existingNote = patchNoteRepository.findByTechAndVersion(tech, version);
                if (existingNote.isPresent()) {
                    log.info("ℹ️ Patch note already exists for {} v{}", techName, version);
                    skippedCount++;
                    continue;
                }

                // Create new patch note
                PatchNote patchNote = new PatchNote(tech, version, url);

                // Set content (limit size for database)
                if (content != null && content.length() > 50) {
                    String truncatedContent = content.length() > 10000 ?
                            content.substring(0, 10000) + "... [truncated]" : content;
                    patchNote.setContent(truncatedContent);
                } else {
                    patchNote.setContent("No content available");
                }

                // Save to database
                PatchNote savedNote = patchNoteRepository.save(patchNote);
                log.info("💾 Saved patch note with ID: {} for {} v{}",
                        savedNote.getId(), techName, version);
                savedCount++;
            }

            String message = String.format("Processing complete! Saved: %d, Skipped: %d, Total: %d",
                    savedCount, skippedCount, crawledData.size());
            log.info("🎉 " + message);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("💥 Processing failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Processing failed: " + e.getMessage());
        }
    }
    
}