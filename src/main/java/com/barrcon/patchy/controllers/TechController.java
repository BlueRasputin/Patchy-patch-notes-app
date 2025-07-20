package com.barrcon.patchy.controllers;


import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.TechRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

//@RestController
//@RequestMapping("api/technologies")
//
//public class TechController {
//
//    @Autowired
//    private TechRepository techRepository;
//
//    @GetMapping
//    public List<Tech> getAllTechnologies() {
//        return techRepository.getallTechnologies();
//    }
//
//    @GetMapping("{id}") public Tech createTechnology(@requestBody Tech tech) {
//        return techRepository.createTech(tech);
//    }
//}
