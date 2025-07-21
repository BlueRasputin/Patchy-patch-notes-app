package com.barrcon.patchy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {

    @postMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {

        return ResponseEntity.ok("User registered successfully");
    }
}
