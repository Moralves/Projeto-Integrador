package com.example.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

    @GetMapping("/api/example")
    public String exampleEndpoint() {
        return "Hello from the ExampleController!";
    }
}