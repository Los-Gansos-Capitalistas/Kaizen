package com.habits.habittracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return """
            {
                "status": "success",
                "message": "🚀 HabitTracker API está funcionando",
                "timestamp": "%s",
                "endpoints": {
                    "root": "GET /",
                    "health": "GET /health", 
                    "auth_register": "POST /api/auth/register",
                    "auth_login": "POST /api/auth/login",
                    "h2_console": "GET /h2-console"
                },
                "controllers_available": [
                    "AuthController",
                    "HabitController", 
                    "ProgressController",
                    "AchievementController",
                    "NotificationController"
                ]
            }
            """.formatted(java.time.LocalDateTime.now());
    }
    
    @GetMapping("/health")
    public String health() {
        return """
            {
                "status": "UP",
                "service": "HabitTracker",
                "timestamp": "%s"
            }
            """.formatted(java.time.LocalDateTime.now());
    }
}