package com.habits.habittracker.service;

import com.habits.habittracker.dto.request.ProgressRequest;
import com.habits.habittracker.dto.response.DailyProgressResponse;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.ProgressRepository;
import com.habits.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ProgressRepository progressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private ProgressService progressService;

    private User usuario;
    private Habit habit;
    private Progress progress;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);
        usuario.setUsername("juan");

        habit = new Habit();
        habit.setId(10L);
        habit.setNombre("Beber agua");
        habit.setUsuario(usuario);

        progress = new Progress();
        progress.setHabito(habit);
        progress.setFecha(LocalDate.now());
        progress.setCumplido(true);
    }

    @Test
    void marcarProgreso_DeberiaCrearONActualizarProgreso() {
        ProgressRequest request = new ProgressRequest();
        request.setFecha(LocalDate.now());
        request.setCumplido(true);

        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));
        when(progressRepository.findByHabitoAndFecha(any(), any())).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenReturn(progress);

        Progress resultado = progressService.marcarProgreso(10L, request);

        assertThat(resultado.getHabito().getNombre()).isEqualTo("Beber agua");
        verify(progressRepository, times(1)).save(any(Progress.class));
    }

    @Test
    void marcarProgreso_DeberiaLanzarExcepcionSiNoExisteHabit() {
        when(habitRepository.findById(anyLong())).thenReturn(Optional.empty());
        ProgressRequest request = new ProgressRequest();
        request.setFecha(LocalDate.now());

        assertThrows(ResourceNotFoundException.class, () -> progressService.marcarProgreso(99L, request));
    }

    @Test
    void obtenerProgresoPorHabito_DeberiaRetornarLista() {
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));
        when(progressRepository.findByHabito(habit)).thenReturn(List.of(progress));

        List<Progress> lista = progressService.obtenerProgresoPorHabito(10L);

        assertThat(lista).hasSize(1);
        verify(progressRepository, times(1)).findByHabito(habit);
    }

    @Test
    void eliminarProgreso_DeberiaEliminarSiExiste() {
        when(progressRepository.existsById(1L)).thenReturn(true);

        progressService.eliminarProgreso(1L);

        verify(progressRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarProgreso_DebeLanzarExcepcionSiNoExiste() {
        when(progressRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> progressService.eliminarProgreso(1L));
    }

    @Test
    void obtenerProgresoDiario_DeberiaRetornarPorcentajeCorrecto() {
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(habitRepository.findByUsuario(usuario)).thenReturn(List.of(habit));
        when(progressRepository.findByHabito(habit)).thenReturn(List.of(progress));

        Map<String, Object> resultado = progressService.obtenerProgresoDiario("juan");

        assertThat(resultado.get("habitosCumplidos")).isEqualTo(1L);
        assertThat(resultado.get("porcentaje")).isEqualTo(100.0);
    }

    @Test
    void obtenerProgresoDiarioDetallado_DeberiaSepararCumplidosYPendientes() {
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(usuario));
        when(habitRepository.findByUsuario(usuario)).thenReturn(List.of(habit));
        when(progressRepository.findByHabitoInAndFecha(anyList(), any())).thenReturn(List.of(progress));

        DailyProgressResponse response = progressService.obtenerProgresoDiarioDetallado("juan", LocalDate.now());

        assertThat(response.getHabitosCumplidos()).contains("Beber agua");
        assertThat(response.getPorcentaje()).isEqualTo(100.0);
    }
}