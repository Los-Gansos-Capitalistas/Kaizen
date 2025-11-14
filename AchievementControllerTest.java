package com.habits.habittracker.controller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Achievement;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;
import com.habits.habittracker.service.AchievementService;

class AchievementControllerTest {

    @Mock
    private AchievementService achievementService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AchievementController achievementController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void getAchievements_DeberiaRetornarListaDeLogros() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Achievement logro = new Achievement();
        logro.setNombre("Primer paso");

        when(achievementService.obtenerLogrosPorUsuario(user)).thenReturn(List.of(logro));

        ResponseEntity<List<Achievement>> response = achievementController.getAchievements(authentication);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getNombre()).isEqualTo("Primer paso");
        verify(achievementService, times(1)).obtenerLogrosPorUsuario(user);
    }

    @Test
    void checkAchievements_DeberiaLlamarServicioYRetornarMensajeExitoso() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        doNothing().when(achievementService).verificarYAsignarLogros(user);

        ResponseEntity<String> response = achievementController.checkAchievements(authentication);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("Verificación completada");
        verify(achievementService, times(1)).verificarYAsignarLogros(user);
    }

    @Test
    void getAchievements_DeberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(authentication.getName()).thenReturn("desconocido");
        when(userRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> achievementController.getAchievements(authentication));

        verify(userRepository, times(1)).findByUsername("desconocido");
    }

    @Test
    void checkAchievements_DeberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(authentication.getName()).thenReturn("desconocido");
        when(userRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> achievementController.checkAchievements(authentication));

        verify(userRepository, times(1)).findByUsername("desconocido");
    }
}
