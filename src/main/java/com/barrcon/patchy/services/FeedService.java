//package com.barrcon.patchy.services;
//
//import com.barrcon.patchy.dto.ProcessedPatchNotesDTO;
//import com.barrcon.patchy.models.Feed;
//import com.barrcon.patchy.models.User;
//import com.barrcon.patchy.repositories.FeedRepository;
//import com.barrcon.patchy.repositories.PatchNotesRepository;
//import com.barrcon.patchy.repositories.UserRepository;
//import org.hibernate.mapping.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class FeedService {
//
//    @AutoWired
//    private GeminiPatchNotesService geminiService;
//
//    @AutoWired
//    private PatchNotesRepository patchNotesRepository;
//
//    @Autowired
//    private FeedRepository feedRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    public List<Feed> generateFeedForUser(Long userId) {
//        User user = userRepository.findById(userId);
//
//
//        for (Long techId : user.getFavoriteTechIds()) {
//            Tech tech = techRepository.findById(techId).orElse(null);
//            if (tech != null) {
//                try {
//
//                    ProcessedPatchNotesDTO processed = geminiService.processPatchNotes(
//                            tech.getVersion(),
//                            tech.getName()
//                    );
//
//                    PatchNotes patchNote = new PatchNotes(
//                            processed.getDescription(),
//                            processed.getVersion(),
//                            processed.getTitle()
//                    );
//                    patchNote.setTech(tech);
//                    PatchNotes savedPatchNote = patchNotesRepository.save(patchNote);
//
//                    Feed feedEntry = new Feed(user, savedPatchNote);
//                    feedEntries.add(feedRepository.save(feedEntry));
//                } catch (Exception e) {
//                    System.err.println("Failed to generate patch notes for tech: " + tech.getName());
//                }
//            }
//
//            return feedEntries;
//}
