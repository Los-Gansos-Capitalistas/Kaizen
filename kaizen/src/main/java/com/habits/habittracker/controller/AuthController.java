package com.habits.habittracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habits.habittracker.dto.request.LoginRequest;
import com.habits.habittracker.dto.request.RegisterRequest;
import com.habits.habittracker.dto.response.AuthResponse;
import com.habits.habittracker.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("""
            {
                "status": "success",
                "service": "HabitTracker Auth API",
                "message": "Servicio de autenticación funcionando correctamente",
                "timestamp": "%s",
                "endpoints_available": [
                    "POST /api/auth/register",
                    "POST /api/auth/login", 
                    "GET /api/auth/status"
                ]
            }
            """.formatted(java.time.LocalDateTime.now()));
    }
}
