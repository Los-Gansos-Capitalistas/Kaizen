package com.habits.habittracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Achievement;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;
import com.habits.habittracker.service.AchievementService;

@RestController
@RequestMapping("/api/achievements")
@CrossOrigin(origins = "*")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Achievement>> getAchievements(Authentication authentication) {
        String username = authentication.getName();
        User usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return ResponseEntity.ok(achievementService.obtenerLogrosPorUsuario(usuario));
    }

    @PostMapping("/check")
    public ResponseEntity<String> checkAchievements(Authentication authentication) {
        String username = authentication.getName();
        User usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        achievementService.verificarYAsignarLogros(usuario);
        return ResponseEntity.ok("Verificación completada. Nuevos logros asignados si correspondía.");
    }
}
