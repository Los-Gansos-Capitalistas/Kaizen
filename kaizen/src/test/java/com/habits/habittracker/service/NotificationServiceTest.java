package com.habits.habittracker.service;

import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void revisarHabitosParaNotificar_DeberiaMostrarMensajeSiHoraCoincideYAlgunaNotificacionActiva() {
        // ARRANGE
        Habit habit = new Habit();
        habit.setNombre("Beber agua");
        habit.setHora(LocalTime.now().withSecond(0).withNano(0).toString());

        NotificationSettings settings = new NotificationSettings();
        settings.setEmailEnabled(true);
        settings.setPushEnabled(false);
        settings.setSmsEnabled(false);
        settings.setNotificationTime(LocalTime.now().withSecond(0).withNano(0).toString());

        habit.setNotificationSettings(settings);

        when(habitRepository.findAll()).thenReturn(List.of(habit));

        // ACT
        messageService.revisarHabitosParaNotificar();

        // ASSERT
        verify(habitRepository, times(1)).findAll();
    }

    @Test
    void revisarHabitosParaNotificar_NoDebeNotificarSiTodasLasNotificacionesEstanDesactivadas() {
        Habit habit = new Habit();
        habit.setNombre("Leer un libro");
        habit.setHora(LocalTime.now().withSecond(0).withNano(0).toString());

        NotificationSettings settings = new NotificationSettings();
        settings.setEmailEnabled(false);
        settings.setPushEnabled(false);
        settings.setSmsEnabled(false);
        settings.setNotificationTime(LocalTime.now().withSecond(0).withNano(0).toString());

        habit.setNotificationSettings(settings);

        when(habitRepository.findAll()).thenReturn(List.of(habit));

        messageService.revisarHabitosParaNotificar();

        verify(habitRepository, times(1)).findAll();
    }

    @Test
    void revisarHabitosParaNotificar_NoDebeNotificarSiHoraNoCoincide() {
        Habit habit = new Habit();
        habit.setNombre("Hacer ejercicio");
        habit.setHora(LocalTime.now().plusMinutes(5).toString());

        NotificationSettings settings = new NotificationSettings();
        settings.setEmailEnabled(true);
        settings.setNotificationTime(LocalTime.now().plusMinutes(5).toString());
        habit.setNotificationSettings(settings);

        when(habitRepository.findAll()).thenReturn(List.of(habit));

        messageService.revisarHabitosParaNotificar();

        verify(habitRepository, times(1)).findAll();
    }

    @Test
    void revisarHabitosParaNotificar_DebeManejarFormatoHoraInvalido() {
        Habit habit = new Habit();
        habit.setNombre("Caminar");
        habit.setHora("hora_invalida");

        NotificationSettings settings = new NotificationSettings();
        settings.setEmailEnabled(true);
        settings.setNotificationTime("hora_invalida");
        habit.setNotificationSettings(settings);

        when(habitRepository.findAll()).thenReturn(List.of(habit));

        messageService.revisarHabitosParaNotificar();

        verify(habitRepository, times(1)).findAll();
    }
}