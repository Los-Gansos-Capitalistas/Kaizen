package com.habits.habittracker.service;

import com.habits.habittracker.dto.request.ProgressRequest;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.ProgressRepository;
import com.habits.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProgressServiceIntegrationTest {

    @Autowired
    private ProgressService progressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private ProgressRepository progressRepository;

    private User usuario;
    private Habit habit;

    @BeforeEach
    void setUp() {
        progressRepository.deleteAll();
        habitRepository.deleteAll();
        userRepository.deleteAll();

        usuario = new User();
        usuario.setUsername("juan");
        usuario.setPassword("1234");
        userRepository.save(usuario);

        habit = new Habit();
        habit.setNombre("Leer");
        habit.setUsuario(usuario);
        habitRepository.save(habit);
    }

    @Test
    void testMarcarYObtenerProgreso() {
        ProgressRequest request = new ProgressRequest();
        request.setFecha(LocalDate.now());
        request.setCumplido(true);

        Progress creado = progressService.marcarProgreso(habit.getId(), request);
        assertThat(creado.isCumplido()).isTrue();

        List<Progress> progresos = progressService.obtenerProgresoPorHabito(habit.getId());
        assertThat(progresos).hasSize(1);
    }

    @Test
    void testObtenerProgresoDiario() {
        Progress p = new Progress();
        p.setHabito(habit);
        p.setFecha(LocalDate.now());
        p.setCumplido(true);
        progressRepository.save(p);

        Map<String, Object> resumen = progressService.obtenerProgresoDiario("juan");
        assertThat(resumen.get("habitosCumplidos")).isEqualTo(1L);
        assertThat(resumen.get("porcentaje")).isEqualTo(100.0);
    }

    @Test
    void testEliminarProgreso() {
        Progress p = new Progress();
        p.setHabito(habit);
        p.setFecha(LocalDate.now());
        p.setCumplido(true);
        progressRepository.save(p);

        progressService.eliminarProgreso(p.getId());
        assertThat(progressRepository.findAll()).isEmpty();
    }
}