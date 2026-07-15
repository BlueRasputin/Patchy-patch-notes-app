package com.barrcon.patchy.controllers;


import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.TechRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tech")
@CrossOrigin(origins = "http://localhost:5173")
public class TechController {

    @Autowired
    private TechRepository techRepository;

    //get full list of tech
    @GetMapping
    public ResponseEntity<List<Tech>> getAllTech() {
        return ResponseEntity.ok((List<Tech>) techRepository.findAll());
    }


    //get tech by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tech> getTechById(@PathVariable Long id) {
         Optional<Tech> tech = techRepository.findById(id);
         return tech.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Add tech to repository
    @PostMapping
    public ResponseEntity<Tech> createTech(@RequestBody Tech tech) {
        return ResponseEntity.ok(techRepository.save(tech));
    }

    //Delete tech from tech table
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTech(@PathVariable Long id) {
        techRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
