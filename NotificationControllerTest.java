package com.habits.habittracker.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.habits.habittracker.exception.UserNotFoundException;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.model.User;
import com.habits.habittracker.service.NotificationService;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationSettings settings;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        settings = new NotificationSettings();
        settings.setId(1L);
        settings.setUsuario(testUser);
        settings.setEmailEnabled(true);
        settings.setPushEnabled(false);
        settings.setSmsEnabled(false);
        settings.setNotificationTime("09:00");
    }

    // ------------------- GET -------------------

    @Test
    void getSettings_DeberiaRetornar200SiUsuarioExiste() {
        when(notificationService.getSettings(1L)).thenReturn(settings);

        ResponseEntity<?> response = notificationController.getSettings(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(settings, response.getBody());
        verify(notificationService).getSettings(1L);
    }

    @Test
    void getSettings_DeberiaRetornar400SiHayArgumentoInvalido() {
        when(notificationService.getSettings(1L))
                .thenThrow(new IllegalArgumentException("ID inválido"));

        ResponseEntity<?> response = notificationController.getSettings(1L);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("ID inválido", response.getBody());
    }

    @Test
    void getSettings_DeberiaRetornar404SiUsuarioNoExiste() {
        // CAMBIO: Pasar 1L en lugar de String
        when(notificationService.getSettings(1L))
                .thenThrow(new UserNotFoundException(1L)); // ← 1L en lugar de String

        ResponseEntity<?> response = notificationController.getSettings(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Usuario con ID 1 no encontrado.", response.getBody()); // ← Mensaje que genera tu excepción
    }

    @Test
    void getSettings_DeberiaRetornar500EnErrorInesperado() {
        when(notificationService.getSettings(1L))
                .thenThrow(new RuntimeException("Fallo general"));

        ResponseEntity<?> response = notificationController.getSettings(1L);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Error al obtener configuración"));
    }

    // ------------------- POST -------------------

    @Test
    void saveSettings_DeberiaGuardarYRetornar200() {
        when(notificationService.saveSettings(1L, settings)).thenReturn(settings);

        ResponseEntity<?> response = notificationController.saveSettings(1L, settings);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(settings, response.getBody());
        verify(notificationService).saveSettings(1L, settings);
    }

    @Test
    void saveSettings_DeberiaRetornar400SiHayErrorDeArgumento() {
        when(notificationService.saveSettings(1L, settings))
                .thenThrow(new IllegalArgumentException("Datos inválidos"));

        ResponseEntity<?> response = notificationController.saveSettings(1L, settings);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Datos inválidos", response.getBody());
    }

    @Test
    void saveSettings_DeberiaRetornar500SiHayErrorDeRuntime() {
        when(notificationService.saveSettings(1L, settings))
                .thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<?> response = notificationController.saveSettings(1L, settings);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Error interno", response.getBody());
    }

    // ------------------- DELETE -------------------

    @Test
    void deleteSettings_DeberiaEliminarYRetornar200() {
        ResponseEntity<?> response = notificationController.deleteSettings(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Configuración de notificaciones eliminada correctamente.", response.getBody());
        verify(notificationService).deleteSettings(1L);
    }

    @Test
    void deleteSettings_DeberiaRetornar404SiUsuarioNoExiste() {
        // CAMBIO: Pasar 1L en lugar de String
        doThrow(new UserNotFoundException(1L)) // ← 1L en lugar de String
                .when(notificationService).deleteSettings(1L);

        ResponseEntity<?> response = notificationController.deleteSettings(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Usuario con ID 1 no encontrado.", response.getBody()); // ← Mensaje que genera tu excepción
    }

    @Test
    void deleteSettings_DeberiaRetornar500SiErrorInterno() {
        doThrow(new RuntimeException("Error al eliminar"))
                .when(notificationService).deleteSettings(1L);

        ResponseEntity<?> response = notificationController.deleteSettings(1L);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Error al eliminar"));
    }

    // ------------------- PUT /status -------------------

    @Test
    void changeNotificationStatus_DeberiaActualizarEstadoYRetornar200() {
        when(notificationService.changeNotificationStatus(1L, true)).thenReturn(settings);

        ResponseEntity<?> response = notificationController.changeNotificationStatus(1L, true);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(settings, response.getBody());
        verify(notificationService).changeNotificationStatus(1L, true);
    }

    @Test
    void changeNotificationStatus_DeberiaRetornar404SiUsuarioNoExiste() {
        // CAMBIO: Pasar 1L en lugar de String
        when(notificationService.changeNotificationStatus(1L, true))
                .thenThrow(new UserNotFoundException(1L)); // ← 1L en lugar de String

        ResponseEntity<?> response = notificationController.changeNotificationStatus(1L, true);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Usuario con ID 1 no encontrado.", response.getBody()); // ← Mensaje que genera tu excepción
    }

    @Test
    void changeNotificationStatus_DeberiaRetornar500SiErrorInterno() {
        when(notificationService.changeNotificationStatus(1L, false))
                .thenThrow(new RuntimeException("Error inesperado"));

        ResponseEntity<?> response = notificationController.changeNotificationStatus(1L, false);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Error al actualizar estado"));
    }

    // ------------------- PUT /reset -------------------

    @Test
    void resetToDefault_DeberiaRestablecerConfiguracionYRetornar200() {
        when(notificationService.resetToDefault(1L)).thenReturn(settings);

        ResponseEntity<?> response = notificationController.resetToDefault(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(settings, response.getBody());
        verify(notificationService).resetToDefault(1L);
    }

    @Test
    void resetToDefault_DeberiaRetornar404SiUsuarioNoExiste() {
        // CAMBIO: Pasar 1L en lugar de String
        when(notificationService.resetToDefault(1L))
                .thenThrow(new UserNotFoundException(1L)); // ← 1L en lugar de String

        ResponseEntity<?> response = notificationController.resetToDefault(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Usuario con ID 1 no encontrado.", response.getBody()); // ← Mensaje que genera tu excepción
    }

    @Test
    void resetToDefault_DeberiaRetornar500SiFalla() {
        when(notificationService.resetToDefault(1L))
                .thenThrow(new RuntimeException("Error al restablecer"));

        ResponseEntity<?> response = notificationController.resetToDefault(1L);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Error al restablecer"));
    }
}