package com.habits.habittracker.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habits.habittracker.dto.request.ProgressRequest;
import com.habits.habittracker.dto.response.DailyProgressResponse;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.service.ProgressService;
import com.habits.habittracker.service.UserService;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @Autowired
    private UserService userService;

    // Crear o actualizar progreso
    @PostMapping("/{habitId}")
    public Progress marcarProgreso(@PathVariable Long habitId, @RequestBody ProgressRequest request) {
        return progressService.marcarProgreso(habitId, request);
    }

    // Obtener progreso por hábito
    @GetMapping("/habit/{habitId}")
    public List<Progress> obtenerPorHabito(@PathVariable Long habitId) {
        return progressService.obtenerProgresoPorHabito(habitId);
    }

    // Eliminar progreso
    @DeleteMapping("/{id}")
    public void eliminarProgreso(@PathVariable Long id) {
        progressService.eliminarProgreso(id);
    }

    // Obtener progreso diario
    @GetMapping("/daily/{username}")
    public Map<String, Object> obtenerProgresoDiario(@PathVariable String username) {
        return progressService.obtenerProgresoDiario(username);
    }

    // Obtener progreso detallado
    @GetMapping("/daily/{username}/{fecha}")
    public DailyProgressResponse obtenerProgresoDetallado(
            @PathVariable String username,
            @PathVariable String fecha) {
        return progressService.obtenerProgresoDiarioDetallado(username, LocalDate.parse(fecha));
    }

    // Estadísticas semanales
    @GetMapping("/weekly/{username}")
    public Map<String, Object> obtenerEstadisticasSemanales(@PathVariable String username) {
        User usuario = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return progressService.obtenerEstadisticasSemanales(usuario);
    }

    // Estadísticas mensuales
    @GetMapping("/monthly/{username}")
    public Map<String, Object> obtenerEstadisticasMensuales(@PathVariable String username) {
        User usuario = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return progressService.obtenerEstadisticasMensuales(usuario);
    }
}
