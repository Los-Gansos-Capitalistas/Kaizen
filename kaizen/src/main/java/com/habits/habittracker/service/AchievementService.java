package com.habits.habittracker.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Achievement;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.AchievementRepository;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.ProgressRepository;

@Service
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private HabitRepository habitRepository;

    public void verificarYAsignarLogros(User usuario) {
        if (usuario == null) {
            throw new ResourceNotFoundException("Usuario no encontrado para verificar logros");
        }

        List<Habit> habitos = habitRepository.findByUsuario(usuario);
        List<Progress> progresos = progressRepository.findAll();

        long totalCumplidos = progresos.stream()
                .filter(Progress::isCumplido)
                .count();

        // Primer paso
        if (totalCumplidos >= 1 && !achievementRepository.existsByUsuarioAndNombre(usuario, "Primer paso")) {
            asignarLogro(usuario, "Primer paso", "Completaste tu primer hábito");
        }

        // Racha de 3 días
        long racha = calcularRachaActual(progresos);
        if (racha >= 3 && !achievementRepository.existsByUsuarioAndNombre(usuario, "Racha de 3 días")) {
            asignarLogro(usuario, "Racha de 3 días", "Mantuviste una racha de 3 días consecutivos");
        }

        // Constancia semanal
        if (totalCumplidos >= 5 && !achievementRepository.existsByUsuarioAndNombre(usuario, "Constancia semanal")) {
            asignarLogro(usuario, "Constancia semanal", "Cumpliste hábitos al menos 5 días esta semana");
        }

        // Maestro de hábitos
        if (totalCumplidos >= 50 && !achievementRepository.existsByUsuarioAndNombre(usuario, "Maestro de hábitos")) {
            asignarLogro(usuario, "Maestro de hábitos", "Completaste 50 hábitos en total");
        }
    }

    private void asignarLogro(User usuario, String nombre, String descripcion) {
        Achievement logro = new Achievement();
        logro.setUsuario(usuario);
        logro.setNombre(nombre);
        logro.setDescripcion(descripcion);
        achievementRepository.save(logro);
    }

    private long calcularRachaActual(List<Progress> progresos) {
        long racha = 0;
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = hoy.minusDays(i);
            boolean cumplidoHoy = progresos.stream()
                    .anyMatch(p -> fecha.equals(p.getFecha()) && p.isCumplido());
            if (cumplidoHoy) {
                racha++;
            } else {
                break;
            }
        }
        return racha;
    }

    public List<Achievement> obtenerLogrosPorUsuario(User usuario) {
        if (usuario == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return achievementRepository.findByUsuario(usuario);
    }
}
