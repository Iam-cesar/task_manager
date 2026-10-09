package com.example.task_manager.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
@Tag(name = "Health", description = "Application health endpoint")
public class HealthCheckController {

    @GetMapping
    public String healthCheck() {
        return "OK";
    }
}
