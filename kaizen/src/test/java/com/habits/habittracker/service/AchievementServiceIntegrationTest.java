package com.habits.habittracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

class AchievementServiceIntegrationTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private AchievementService achievementService;

    private User usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = new User();
        usuario.setId(1L);
        usuario.setNombre("Juan");
    }

    @Test
    void verificarYAsignarLogros_DeberiaAsignarPrimerPasoYRachaYConstancia() {
        // Habito y progreso simulados
        Habit habit = new Habit();
        habit.setUsuario(usuario);

        List<Habit> habitos = List.of(habit);
        when(habitRepository.findByUsuario(usuario)).thenReturn(habitos);

        // 5 progresos cumplidos (para activar hasta “Constancia semanal”)
        List<Progress> progresos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Progress p = new Progress();
            p.setFecha(LocalDate.now().minusDays(i));
            p.setCumplido(true);
            progresos.add(p);
        }
        when(progressRepository.findAll()).thenReturn(progresos);
        when(achievementRepository.existsByUsuarioAndNombre(any(), anyString())).thenReturn(false);

        achievementService.verificarYAsignarLogros(usuario);

        // Verifica que se hayan guardado los logros esperados
        verify(achievementRepository, times(3)).save(any(Achievement.class));
    }

    @Test
    void verificarYAsignarLogros_DeberiaAsignarMaestroDeHabitos() {
        List<Habit> habitos = List.of(new Habit());
        when(habitRepository.findByUsuario(usuario)).thenReturn(habitos);

        // 55 progresos cumplidos
        List<Progress> progresos = new ArrayList<>();
        for (int i = 0; i < 55; i++) {
            Progress p = new Progress();
            p.setFecha(LocalDate.now().minusDays(i));
            p.setCumplido(true);
            progresos.add(p);
        }

        when(progressRepository.findAll()).thenReturn(progresos);
        when(achievementRepository.existsByUsuarioAndNombre(any(), anyString())).thenReturn(false);

        achievementService.verificarYAsignarLogros(usuario);

        // Debe guardar los 4 logros (Primer paso, Racha 3 días, Constancia semanal, Maestro de hábitos)
        verify(achievementRepository, times(4)).save(any(Achievement.class));
    }

    @Test
    void verificarYAsignarLogros_UsuarioNullDebeLanzarExcepcion() {
        assertThrows(ResourceNotFoundException.class, () -> {
            achievementService.verificarYAsignarLogros(null);
        });
        verifyNoInteractions(achievementRepository, habitRepository, progressRepository);
    }

    @Test
    void verificarYAsignarLogros_SinProgresosNoAsignaLogros() {
        when(habitRepository.findByUsuario(usuario)).thenReturn(List.of());
        when(progressRepository.findAll()).thenReturn(Collections.emptyList());

        achievementService.verificarYAsignarLogros(usuario);
        verify(achievementRepository, never()).save(any(Achievement.class));
    }

    @Test
    void obtenerLogrosPorUsuario_DeberiaRetornarLista() {
        List<Achievement> lista = List.of(new Achievement(), new Achievement());
        when(achievementRepository.findByUsuario(usuario)).thenReturn(lista);

        List<Achievement> resultado = achievementService.obtenerLogrosPorUsuario(usuario);

        assertThat(resultado).hasSize(2);
        verify(achievementRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    void obtenerLogrosPorUsuario_UsuarioNullDebeLanzarExcepcion() {
        assertThrows(ResourceNotFoundException.class, () -> {
            achievementService.obtenerLogrosPorUsuario(null);
        });
        verifyNoInteractions(achievementRepository);
    }

    @Test
    void calcularRachaActual_DeberiaContarSoloConsecutivos() {
        // Se llama indirectamente a través de verificarYAsignarLogros
        Habit habit = new Habit();
        habit.setUsuario(usuario);
        when(habitRepository.findByUsuario(usuario)).thenReturn(List.of(habit));

        List<Progress> progresos = new ArrayList<>();
        progresos.add(crearProgreso(LocalDate.now(), true));
        progresos.add(crearProgreso(LocalDate.now().minusDays(1), true));
        progresos.add(crearProgreso(LocalDate.now().minusDays(2), false)); // rompe la racha
        progresos.add(crearProgreso(LocalDate.now().minusDays(3), true));
        when(progressRepository.findAll()).thenReturn(progresos);
        when(achievementRepository.existsByUsuarioAndNombre(any(), anyString())).thenReturn(false);

        achievementService.verificarYAsignarLogros(usuario);

        // Solo debe otorgar el logro “Primer paso”
        verify(achievementRepository, times(1)).save(any(Achievement.class));
    }

    private Progress crearProgreso(LocalDate fecha, boolean cumplido) {
        Progress p = new Progress();
        p.setFecha(fecha);
        p.setCumplido(cumplido);
        return p;
    }
}
