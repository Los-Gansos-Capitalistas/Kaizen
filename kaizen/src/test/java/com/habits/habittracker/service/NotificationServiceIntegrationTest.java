package com.habits.habittracker.service;

import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.exception.UserNotFoundException;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.NotificationSettingsRepository;
import com.habits.habittracker.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceIntegrationTest {

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User usuario;
    private NotificationSettings settings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = new User();
        usuario.setId(1L);
        usuario.setNombre("Juan Pablo");

        settings = NotificationSettings.builder()
                .id(1L)
                .usuario(usuario)
                .emailEnabled(true)
                .pushEnabled(false)
                .smsEnabled(false)
                .notificationTime("09:00")
                .build();
    }

    // ✅ GET Settings
    @Test
    void getSettings_DeberiaRetornarConfiguracionExistente() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificationSettingsRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(settings));

        NotificationSettings result = notificationService.getSettings(1L);

        assertThat(result).isNotNull();
        assertThat(result.getNotificationTime()).isEqualTo("09:00");
        verify(notificationSettingsRepository, times(1)).findByUsuario_Id(1L);
    }

    @Test
    void getSettings_DeberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> notificationService.getSettings(1L));
    }

    @Test
    void getSettings_DeberiaLanzarExcepcionSiNoHayConfiguracion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificationSettingsRepository.findByUsuario_Id(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> notificationService.getSettings(1L));
    }

    // ✅ SAVE Settings
    @Test
    void saveSettings_DeberiaGuardarONuevoRegistro() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificationSettingsRepository.findByUsuario_Id(1L)).thenReturn(Optional.empty());
        when(notificationSettingsRepository.save(any(NotificationSettings.class))).thenReturn(settings);

        NotificationSettings newSettings = NotificationSettings.builder()
                .emailEnabled(false)
                .pushEnabled(true)
                .smsEnabled(true)
                .notificationTime("07:30")
                .build();

        NotificationSettings result = notificationService.saveSettings(1L, newSettings);

        assertThat(result).isNotNull();
        assertThat(result.getNotificationTime()).isEqualTo("09:00"); // el mock retorna settings original
        verify(notificationSettingsRepository, times(1)).save(any(NotificationSettings.class));
    }

    @Test
    void saveSettings_DeberiaLanzarExcepcionSiUsuarioNull() {
        assertThrows(BadRequestException.class, () -> notificationService.saveSettings(null, settings));
    }

    @Test
    void saveSettings_DeberiaLanzarExcepcionSiSettingsNull() {
        assertThrows(BadRequestException.class, () -> notificationService.saveSettings(1L, null));
    }

    // ✅ DELETE Settings
    @Test
    void deleteSettings_DeberiaEliminarConfiguracion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificationSettingsRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(settings));

        notificationService.deleteSettings(1L);

        verify(notificationSettingsRepository, times(1)).delete(settings);
    }

    @Test
    void deleteSettings_SinConfiguracionDebeLanzarExcepcion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificationSettingsRepository.findByUsuario_Id(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.deleteSettings(1L));
    }

    // ✅ Cambiar estado global
    @Test
    void changeNotificationStatus_DeberiaActualizarEstados() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificationSettingsRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(settings));
        when(notificationSettingsRepository.save(any(NotificationSettings.class))).thenReturn(settings);

        NotificationSettings result = notificationService.changeNotificationStatus(1L, true);

        assertThat(result.getEmailEnabled()).isTrue();
        verify(notificationSettingsRepository, times(1)).save(any(NotificationSettings.class));
    }

    // ✅ Reset a valores por defecto
    @Test
    void resetToDefault_DeberiaGuardarConfiguracionPorDefecto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificationSettingsRepository.save(any(NotificationSettings.class))).thenReturn(settings);

        NotificationSettings result = notificationService.resetToDefault(1L);

        assertThat(result).isNotNull();
        assertThat(result.getNotificationTime()).isEqualTo("09:00");
        verify(notificationSettingsRepository, times(1)).save(any(NotificationSettings.class));
    }

    @Test
    void resetToDefault_UsuarioInexistenteDebeLanzarExcepcion() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> notificationService.resetToDefault(1L));
    }
}