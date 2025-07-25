package com.barrcon.patchy.controllers;

import com.barrcon.patchy.models.Feed;
import com.barrcon.patchy.models.User;
import com.barrcon.patchy.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import com.barrcon.patchy.repositories.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/feed")
@CrossOrigin
public class FeedController {

    @Autowired
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    public FeedController(FeedRepository feedRepository, UserRepository userRepository) {
        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feed>> getUserFeed(@PathVariable Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<Feed> feeds = feedRepository.findByUserIdAndTechIdIn(userId, user.getFavoriteTechIds());
            return ResponseEntity.ok(feeds);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Feed>> getAllFeeds() {
        return ResponseEntity.ok((List<Feed>) feedRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feed> getFeedById(@PathVariable Long id) {
        Optional<Feed> feed = feedRepository.findById(id);
        return feed.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Feed> createFeed(@RequestBody Feed feed) {
        return ResponseEntity.ok(feedRepository.save(feed));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Feed> markAsRead(@PathVariable Long id) {
        Optional<Feed> feedOptional = feedRepository.findById(id);
        if (feedOptional.isPresent()) {
            Feed feed = feedOptional.get();
            feed.setRead(true);
            return ResponseEntity.ok(feedRepository.save(feed));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long id) {
        feedRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}