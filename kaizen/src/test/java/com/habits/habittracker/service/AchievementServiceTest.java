package com.habits.habittracker.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Achievement;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.AchievementRepository;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.ProgressRepository;

class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private AchievementService achievementService;

    private User usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = new User();
        usuario.setId(1L);
        usuario.setNombre("TestUser");
    }

    @Test
    void testVerificarYAsignarLogros_TodosLosLogros() {
        // Datos de prueba: 50 progresos cumplidos + racha de 3 días
        List<Progress> progresos = generarProgresosCumplidos(50, 3);

        when(habitRepository.findByUsuario(usuario)).thenReturn(Collections.singletonList(new Habit()));
        when(progressRepository.findAll()).thenReturn(progresos);
        when(achievementRepository.existsByUsuarioAndNombre(eq(usuario), any())).thenReturn(false);

        achievementService.verificarYAsignarLogros(usuario);

        // Verifica que se hayan asignado los 4 logros posibles
        verify(achievementRepository, times(4)).save(any(Achievement.class));
    }

    @Test
    void testVerificarYAsignarLogros_Parcial() {
        // Solo 2 progresos → solo "Primer paso" debe cumplirse
        List<Progress> progresos = generarProgresosCumplidos(2, 1);

        when(habitRepository.findByUsuario(usuario)).thenReturn(Collections.singletonList(new Habit()));
        when(progressRepository.findAll()).thenReturn(progresos);
        when(achievementRepository.existsByUsuarioAndNombre(eq(usuario), any())).thenReturn(false);

        achievementService.verificarYAsignarLogros(usuario);

        verify(achievementRepository, times(1)).save(any(Achievement.class));
    }

    @Test
    void testVerificarYAsignarLogros_SinUsuario() {
        org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class,
                () -> achievementService.verificarYAsignarLogros(null));
    }

    @Test
    void testObtenerLogrosPorUsuario() {
        List<Achievement> logros = Arrays.asList(new Achievement(), new Achievement());
        when(achievementRepository.findByUsuario(usuario)).thenReturn(logros);

        List<Achievement> result = achievementService.obtenerLogrosPorUsuario(usuario);
        org.junit.jupiter.api.Assertions.assertEquals(2, result.size());
    }

    @Test
    void testObtenerLogrosPorUsuario_Null() {
        org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class,
                () -> achievementService.obtenerLogrosPorUsuario(null));
    }

    private List<Progress> generarProgresosCumplidos(int total, int rachaDias) {
        LocalDate hoy = LocalDate.now();
        Progress[] progresos = new Progress[total];
        for (int i = 0; i < total; i++) {
            Progress p = new Progress();
            p.setCumplido(true);
            if (i < rachaDias) {
                p.setFecha(hoy.minusDays(i));
            } else {
                p.setFecha(hoy.minusDays(rachaDias + i));
            }
            progresos[i] = p;
        }
        return Arrays.asList(progresos);
    }
}
