package com.habits.habittracker.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.habits.habittracker.dto.request.HabitRequest;
import com.habits.habittracker.dto.response.HabitResponse;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;
import com.habits.habittracker.service.HabitService;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    @Mock
    private HabitService habitService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HabitController habitController;

    private User testUser;
    private Habit testHabit;
    private HabitRequest testHabitRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setNombre("Test Habit");
        testHabit.setCategoria("Test Category");
        testHabit.setFrecuencia("Diaria");
        testHabit.setHora("08:00");
        testHabit.setDescripcion("Test Description");
        testHabit.setFechaObjetivo(LocalDate.of(2025, 12, 31));
        testHabit.setUsuario(testUser);

        testHabitRequest = new HabitRequest();
        testHabitRequest.setNombre("Test Habit");
        testHabitRequest.setCategoria("Test Category");
        testHabitRequest.setFrecuencia("Diaria");
        testHabitRequest.setHora("08:00");
        testHabitRequest.setDescripcion("Test Description");
        testHabitRequest.setFechaObjetivo(LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("Get all habits should return list of habits when user is authenticated")
    void testGetAllHabits_Success() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(habitService.getHabitsByUser(testUser)).thenReturn(Arrays.asList(testHabit));

        List<HabitResponse> result = habitController.getAllHabits(authentication);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Habit", result.get(0).getNombre());
        assertEquals(LocalDate.of(2025, 12, 31), result.get(0).getFechaObjetivo());

        verify(authentication).getName();
        verify(userRepository).findByUsername("testuser");
        verify(habitService).getHabitsByUser(testUser);
    }

    @Test
    @DisplayName("Get all habits should throw exception when user is not authenticated")
    void testGetAllHabits_Unauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(BadRequestException.class, () -> {
            habitController.getAllHabits(authentication);
        });

        verify(authentication).isAuthenticated();
    }

    @Test
    @DisplayName("Get all habits should throw exception when user not found")
    void testGetAllHabits_UserNotFound() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            habitController.getAllHabits(authentication);
        });

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Get habit by id should return habit when exists")
    void testGetHabitById_Success() {
        when(habitService.getHabitById(1L)).thenReturn(Optional.of(testHabit));

        ResponseEntity<HabitResponse> response = habitController.getHabitById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Habit", response.getBody().getNombre());
        assertEquals(LocalDate.of(2025, 12, 31), response.getBody().getFechaObjetivo());

        verify(habitService).getHabitById(1L);
    }

    @Test
    @DisplayName("Get habit by id should throw exception when habit not found")
    void testGetHabitById_NotFound() {
        when(habitService.getHabitById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            habitController.getHabitById(1L);
        });

        verify(habitService).getHabitById(1L);
    }

    @Test
    @DisplayName("Create habit should return created habit when valid request")
    void testCreateHabit_Success() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(habitService.createHabit(any(Habit.class))).thenReturn(testHabit);

        ResponseEntity<HabitResponse> response = habitController.createHabit(testHabitRequest, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Habit", response.getBody().getNombre());
        assertEquals(LocalDate.of(2025, 12, 31), response.getBody().getFechaObjetivo());

        verify(authentication).getName();
        verify(userRepository).findByUsername("testuser");
        verify(habitService).createHabit(any(Habit.class));
    }

    @Test
    @DisplayName("Create habit should throw exception when user not authenticated")
    void testCreateHabit_Unauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(BadRequestException.class, () -> {
            habitController.createHabit(testHabitRequest, authentication);
        });

        verify(authentication).isAuthenticated();
    }

    @Test
    @DisplayName("Create habit should throw exception when user not found")
    void testCreateHabit_UserNotFound() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            habitController.createHabit(testHabitRequest, authentication);
        });

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Update habit should return updated habit when valid request")
    void testUpdateHabit_Success() {
        when(habitService.updateHabit(anyLong(), any(HabitRequest.class))).thenReturn(testHabit);

        ResponseEntity<HabitResponse> response = habitController.updateHabit(1L, testHabitRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Habit", response.getBody().getNombre());

        verify(habitService).updateHabit(1L, testHabitRequest);
    }

    @Test
    @DisplayName("Update habit should propagate exception when service fails")
    void testUpdateHabit_ServiceException() {
        when(habitService.updateHabit(anyLong(), any(HabitRequest.class)))
            .thenThrow(new ResourceNotFoundException("Habit not found"));

        assertThrows(ResourceNotFoundException.class, () -> {
            habitController.updateHabit(1L, testHabitRequest);
        });

        verify(habitService).updateHabit(1L, testHabitRequest);
    }

    @Test
    @DisplayName("Delete habit should return no content when successful")
    void testDeleteHabit_Success() {
        doNothing().when(habitService).deleteHabit(1L);

        ResponseEntity<Void> response = habitController.deleteHabit(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(habitService).deleteHabit(1L);
    }

    @Test
    @DisplayName("Delete habit should propagate exception when service fails")
    void testDeleteHabit_ServiceException() {
        doThrow(new ResourceNotFoundException("Habit not found"))
            .when(habitService).deleteHabit(1L);

        assertThrows(ResourceNotFoundException.class, () -> {
            habitController.deleteHabit(1L);
        });

        verify(habitService).deleteHabit(1L);
    }

    @Test
    @DisplayName("Mark habit as completed should return habit when successful")
    void testMarkHabitAsCompleted_Success() {
        when(habitService.getHabitById(1L)).thenReturn(Optional.of(testHabit));

        ResponseEntity<HabitResponse> response = habitController.markHabitAsCompleted(1L, 
            java.util.Map.of("completado", true));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Habit", response.getBody().getNombre());

        verify(habitService).getHabitById(1L);
    }

    @Test
    @DisplayName("Mark habit as completed should throw exception when habit not found")
    void testMarkHabitAsCompleted_NotFound() {
        when(habitService.getHabitById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            habitController.markHabitAsCompleted(1L, java.util.Map.of("completado", true));
        });

        verify(habitService).getHabitById(1L);
    }

    @Test
    @DisplayName("Mark habit as completed should work with false value")
    void testMarkHabitAsCompleted_FalseValue() {
        when(habitService.getHabitById(1L)).thenReturn(Optional.of(testHabit));

        ResponseEntity<HabitResponse> response = habitController.markHabitAsCompleted(1L, 
            java.util.Map.of("completado", false));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(habitService).getHabitById(1L);
    }

    @Test
    @DisplayName("Create habit should handle null fechaObjetivo correctly")
    void testCreateHabit_WithNullFechaObjetivo() {
        testHabitRequest.setFechaObjetivo(null);
        testHabit.setFechaObjetivo(null);

        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(habitService.createHabit(any(Habit.class))).thenReturn(testHabit);

        ResponseEntity<HabitResponse> response = habitController.createHabit(testHabitRequest, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getFechaObjetivo());

        verify(habitService).createHabit(any(Habit.class));
    }

    @Test
    @DisplayName("Get all habits should return empty list when user has no habits")
    void testGetAllHabits_EmptyList() {
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(habitService.getHabitsByUser(testUser)).thenReturn(Arrays.asList());

        List<HabitResponse> result = habitController.getAllHabits(authentication);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(habitService).getHabitsByUser(testUser);
    }

    @Test
    @DisplayName("Update habit should handle partial updates correctly")
    void testUpdateHabit_PartialUpdate() {
        HabitRequest partialUpdate = new HabitRequest();
        partialUpdate.setNombre("Updated Name");
        // Other fields are null

        Habit updatedHabit = new Habit();
        updatedHabit.setId(1L);
        updatedHabit.setNombre("Updated Name");
        updatedHabit.setCategoria("Test Category"); // Should remain unchanged
        updatedHabit.setFrecuencia("Diaria");
        updatedHabit.setHora("08:00");
        updatedHabit.setDescripcion("Test Description");
        updatedHabit.setFechaObjetivo(LocalDate.of(2025, 12, 31));

        when(habitService.updateHabit(anyLong(), any(HabitRequest.class))).thenReturn(updatedHabit);

        ResponseEntity<HabitResponse> response = habitController.updateHabit(1L, partialUpdate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Name", response.getBody().getNombre());
        assertEquals("Test Category", response.getBody().getCategoria()); // Should remain from original

        verify(habitService).updateHabit(1L, partialUpdate);
    }
}