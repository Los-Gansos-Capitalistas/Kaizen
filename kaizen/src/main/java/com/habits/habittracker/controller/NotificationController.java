package com.habits.habittracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.habits.habittracker.exception.UserNotFoundException;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getSettings(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(notificationService.getSettings(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserNotFoundException e) { // 👈 cambio leve aquí
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener configuración: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> saveSettings(@PathVariable Long userId, @RequestBody NotificationSettings settings) {
        try {
            return ResponseEntity.ok(notificationService.saveSettings(userId, settings));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteSettings(@PathVariable Long userId) {
        try {
            notificationService.deleteSettings(userId);
            return ResponseEntity.ok("Configuración de notificaciones eliminada correctamente.");
        } catch (UserNotFoundException e) { // 👈 cambio leve aquí
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> changeNotificationStatus(@PathVariable Long userId, @RequestParam boolean enabled) {
        try {
            return ResponseEntity.ok(notificationService.changeNotificationStatus(userId, enabled));
        } catch (UserNotFoundException e) { // opcional si lo deseas más consistente
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/reset")
    public ResponseEntity<?> resetToDefault(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(notificationService.resetToDefault(userId));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al restablecer: " + e.getMessage());
        }
    }
}
