package com.habits.habittracker.service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.NotificationSettingsRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @InjectMocks
    private MessageService messageService;

    private User usuario;
    private Habit habitConHora;
    private Habit habitSinHora;
    private Habit habitUsuarioNull;
    private Habit habitHoraInvalida;
    private NotificationSettings configActiva;
    private NotificationSettings configInactiva;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);
        usuario.setUsername("testuser");

        habitConHora = new Habit();
        habitConHora.setId(1L);
        habitConHora.setNombre("Ejercicio");
        habitConHora.setHora("08:00");
        habitConHora.setCategoria("Salud");
        habitConHora.setUsuario(usuario);

        habitSinHora = new Habit();
        habitSinHora.setId(2L);
        habitSinHora.setNombre("Meditación");
        habitSinHora.setHora(null);
        habitSinHora.setUsuario(usuario);

        habitUsuarioNull = new Habit();
        habitUsuarioNull.setId(3L);
        habitUsuarioNull.setNombre("Hábito sin usuario");
        habitUsuarioNull.setHora("09:00");
        habitUsuarioNull.setUsuario(null);

        habitHoraInvalida = new Habit();
        habitHoraInvalida.setId(4L);
        habitHoraInvalida.setNombre("Hábito hora inválida");
        habitHoraInvalida.setHora("hora-invalida");
        habitHoraInvalida.setUsuario(usuario);

        configActiva = new NotificationSettings();
        configActiva.setId(1L);
        configActiva.setUsuario(usuario);
        configActiva.setEmailEnabled(true);
        configActiva.setPushEnabled(false);
        configActiva.setSmsEnabled(true);

        configInactiva = new NotificationSettings();
        configInactiva.setId(2L);
        configInactiva.setUsuario(usuario);
        configInactiva.setEmailEnabled(false);
        configInactiva.setPushEnabled(false);
        configInactiva.setSmsEnabled(false);
    }

    @Test
    void revisarHabitosParaNotificar_ConHabitoyHoraCoincidente_DeberiaMostrarNotificacion() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitConHora);
        when(habitRepository.findAll()).thenReturn(habitos);
        when(notificationSettingsRepository.findByUsuario_Id(usuario.getId()))
                .thenReturn(Optional.of(configActiva));

        MessageService serviceSpy = spy(messageService);
        doReturn(LocalTime.of(8, 0)).when(serviceSpy).getCurrentTime();

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, times(1)).findByUsuario_Id(usuario.getId());
    }

    @Test
    void revisarHabitosParaNotificar_ConHabitoyHoraNoCoincidente_NoDeberiaMostrarNotificacion() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitConHora);
        when(habitRepository.findAll()).thenReturn(habitos);
        when(notificationSettingsRepository.findByUsuario_Id(usuario.getId()))
                .thenReturn(Optional.of(configActiva));

        MessageService serviceSpy = spy(messageService);
        doReturn(LocalTime.of(9, 0)).when(serviceSpy).getCurrentTime();

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, times(1)).findByUsuario_Id(usuario.getId());
    }

    @Test
    void revisarHabitosParaNotificar_ConHabitoSinHora_NoDeberiaProcesarNotificacion() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitSinHora);
        when(habitRepository.findAll()).thenReturn(habitos);

        // Act
        messageService.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, never()).findByUsuario_Id(anyLong());
    }

    @Test
    void revisarHabitosParaNotificar_ConHabitoUsuarioNull_NoDeberiaProcesarNotificacion() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitUsuarioNull);
        when(habitRepository.findAll()).thenReturn(habitos);

        // Act
        messageService.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, never()).findByUsuario_Id(anyLong());
    }

    @Test
    void revisarHabitosParaNotificar_ConConfiguracionNotificacionesInactiva_NoDeberiaMostrarNotificacion() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitConHora);
        when(habitRepository.findAll()).thenReturn(habitos);
        when(notificationSettingsRepository.findByUsuario_Id(usuario.getId()))
                .thenReturn(Optional.of(configInactiva));

        MessageService serviceSpy = spy(messageService);
        doReturn(LocalTime.of(8, 0)).when(serviceSpy).getCurrentTime();

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, times(1)).findByUsuario_Id(usuario.getId());
    }

    @Test
    void revisarHabitosParaNotificar_ConUsuarioSinConfiguracion_NoDeberiaMostrarNotificacion() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitConHora);
        when(habitRepository.findAll()).thenReturn(habitos);
        when(notificationSettingsRepository.findByUsuario_Id(usuario.getId()))
                .thenReturn(Optional.empty());

        // Act
        messageService.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, times(1)).findByUsuario_Id(usuario.getId());
    }

    @Test
    void revisarHabitosParaNotificar_ConHoraFormatoInvalido_DeberiaManejarExcepcion() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitHoraInvalida);
        when(habitRepository.findAll()).thenReturn(habitos);
        when(notificationSettingsRepository.findByUsuario_Id(usuario.getId()))
                .thenReturn(Optional.of(configActiva));

        // Act
        messageService.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, times(1)).findByUsuario_Id(usuario.getId());
    }

    @Test
    void revisarHabitosParaNotificar_ConListaVacia_NoDeberiaHacerNada() {
        // Arrange
        when(habitRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        messageService.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, never()).findByUsuario_Id(anyLong());
    }

    @Test
    void revisarHabitosParaNotificar_ConMultiplesHabitos_DeberiaProcesarTodos() {
        // Arrange
        Habit habit2 = new Habit();
        habit2.setId(5L);
        habit2.setNombre("Leer");
        habit2.setHora("10:00");
        habit2.setUsuario(usuario);

        List<Habit> habitos = Arrays.asList(habitConHora, habitSinHora, habitUsuarioNull, habit2);
        when(habitRepository.findAll()).thenReturn(habitos);
        when(notificationSettingsRepository.findByUsuario_Id(usuario.getId()))
                .thenReturn(Optional.of(configActiva));

        MessageService serviceSpy = spy(messageService);
        doReturn(LocalTime.of(8, 0)).when(serviceSpy).getCurrentTime();

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, times(2)).findByUsuario_Id(usuario.getId());
    }

    @Test
    void verificarNotificacionesEnTiempoEspecifico_DeberiaFuncionarCorrectamente() {
        // Arrange
        List<Habit> habitos = Arrays.asList(habitConHora);
        when(habitRepository.findAll()).thenReturn(habitos);
        when(notificationSettingsRepository.findByUsuario_Id(usuario.getId()))
                .thenReturn(Optional.of(configActiva));

        LocalTime tiempoEspecifico = LocalTime.of(8, 0);

        // Act
        messageService.verificarNotificacionesEnTiempoEspecifico(tiempoEspecifico);

        // Assert
        verify(habitRepository, times(1)).findAll();
        verify(notificationSettingsRepository, times(1)).findByUsuario_Id(usuario.getId());
    }

    @Test
    void verificarNotificacionesManual_DeberiaEjecutarRevisarHabitosParaNotificar() {
        // Arrange
        MessageService serviceSpy = spy(messageService);
        doNothing().when(serviceSpy).revisarHabitosParaNotificar();

        // Act
        serviceSpy.verificarNotificacionesManual();

        // Assert
        verify(serviceSpy, times(1)).revisarHabitosParaNotificar();
    }

    @Test
    void getCurrentTime_DeberiaRetornarHoraSinSegundos() {
        // Act
        LocalTime currentTime = messageService.getCurrentTime();

        // Assert
        assertEquals(0, currentTime.getSecond());
        assertEquals(0, currentTime.getNano());
    }
}