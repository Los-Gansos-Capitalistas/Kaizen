package com.habits.habittracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.habits.habittracker.dto.request.HabitRequest;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.UserRepository;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    @Autowired
    public HabitService(HabitRepository habitRepository, UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    public List<Habit> getHabitsByUser(User user) {
        return habitRepository.findByUsuario(user);
    }

    public Optional<Habit> getHabitById(Long id) {
        return habitRepository.findById(id);
    }

    public Habit createHabit(Habit habit) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((UserDetails) principal).getUsername();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            habit.setUsuario(user);
            return habitRepository.save(habit);
        } catch (Exception e) {
            throw new BadRequestException("No se pudo crear el hábito: " + e.getMessage());
        }
    }

    public Habit updateHabit(Long id, HabitRequest habitDetails) {
    return habitRepository.findById(id)
            .map(habit -> {
                System.out.println("=== EN HABIT SERVICE ===");
                System.out.println("HabitDetails fechaObjetivo: " + habitDetails.getFechaObjetivo());
                System.out.println("Habit actual ANTES: " + habit.getFechaObjetivo());
                
                // Actualizar campos - CORREGIDO: fechaObjetivo debe actualizarse incluso si es null
                habit.setNombre(habitDetails.getNombre() != null ? habitDetails.getNombre() : habit.getNombre());
                habit.setCategoria(habitDetails.getCategoria() != null ? habitDetails.getCategoria() : habit.getCategoria());
                habit.setFrecuencia(habitDetails.getFrecuencia() != null ? habitDetails.getFrecuencia() : habit.getFrecuencia());
                habit.setHora(habitDetails.getHora() != null ? habitDetails.getHora() : habit.getHora());
                habit.setDescripcion(habitDetails.getDescripcion() != null ? habitDetails.getDescripcion() : habit.getDescripcion());
                
                // CORRECCIÓN CRÍTICA: fechaObjetivo debe actualizarse siempre, incluso con null
                habit.setFechaObjetivo(habitDetails.getFechaObjetivo());
                
                System.out.println("Habit actual DESPUÉS: " + habit.getFechaObjetivo());
                System.out.println("=== FIN HABIT SERVICE ===");
                
                return habitRepository.save(habit);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado con id: " + id));
}

    public void deleteHabit(Long id) {
        if (!habitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hábito no encontrado con id: " + id);
        }
        habitRepository.deleteById(id);
    }
}