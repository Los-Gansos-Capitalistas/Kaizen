package com.habits.habittracker.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habits.habittracker.dto.request.HabitRequest;
import com.habits.habittracker.dto.response.HabitResponse;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;
import com.habits.habittracker.service.HabitService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(origins = "*")
public class HabitController {

    @Autowired
    private HabitService habitService;

    @Autowired
    private UserRepository userRepository;

    // Obtener todos los hábitos del usuario autenticado
    @GetMapping
    public List<HabitResponse> getAllHabits(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Usuario no autenticado");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return habitService.getHabitsByUser(user)
                .stream()
                .map(habit -> new HabitResponse(
                        habit.getId(),
                        habit.getNombre(),
                        habit.getCategoria(),
                        habit.getFrecuencia(),
                        habit.getHora(),
                        habit.getDescripcion(),
                        habit.getFechaObjetivo()
                ))
                .collect(Collectors.toList());
    }

    // Obtener un hábito por ID
    @GetMapping("/{id}")
    public ResponseEntity<HabitResponse> getHabitById(@PathVariable Long id) {
        Habit habit = habitService.getHabitById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado con id: " + id));

        HabitResponse response = new HabitResponse(
                habit.getId(),
                habit.getNombre(),
                habit.getCategoria(),
                habit.getFrecuencia(),
                habit.getHora(),
                habit.getDescripcion(),
                habit.getFechaObjetivo()
        );
        return ResponseEntity.ok(response);
    }

    // Crear nuevo hábito
    @PostMapping
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody HabitRequest request, Authentication authentication) {
        Habit habit = new Habit();
        habit.setNombre(request.getNombre());
        habit.setCategoria(request.getCategoria());
        habit.setFrecuencia(request.getFrecuencia());
        habit.setHora(request.getHora());
        habit.setDescripcion(request.getDescripcion());
        habit.setFechaObjetivo(request.getFechaObjetivo());

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            habit.setUsuario(user);
        } else {
            throw new BadRequestException("Usuario no autenticado o sesión inválida");
        }

        Habit saved = habitService.createHabit(habit);
        HabitResponse response = new HabitResponse(
                saved.getId(),
                saved.getNombre(),
                saved.getCategoria(),
                saved.getFrecuencia(),
                saved.getHora(),
                saved.getDescripcion(),
                saved.getFechaObjetivo()
        );
        return ResponseEntity.ok(response);
    }

    // Actualizar hábito
    @PutMapping("/{id}")
    public ResponseEntity<HabitResponse> updateHabit(@PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        Habit updated = habitService.updateHabit(id, request);
        HabitResponse response = new HabitResponse(
                updated.getId(),
                updated.getNombre(),
                updated.getCategoria(),
                updated.getFrecuencia(),
                updated.getHora(),
                updated.getDescripcion(),
                updated.getFechaObjetivo()
        );
        return ResponseEntity.ok(response);
    }

    // Eliminar hábito
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    // ENDPOINT NUEVO - Marcar hábito como completado
    @PostMapping("/{id}/complete")
    public ResponseEntity<HabitResponse> markHabitAsCompleted(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Habit habit = habitService.getHabitById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado con id: " + id));
        
        Boolean completado = request.get("completado");
        if (completado != null) {
            // Aquí puedes implementar la lógica para guardar el progreso
            System.out.println("Marcando hábito " + id + " como completado: " + completado);
        }
        
        HabitResponse response = new HabitResponse(
                habit.getId(),
                habit.getNombre(),
                habit.getCategoria(),
                habit.getFrecuencia(),
                habit.getHora(),
                habit.getDescripcion(),
                habit.getFechaObjetivo()
        );
        return ResponseEntity.ok(response);
    }
}