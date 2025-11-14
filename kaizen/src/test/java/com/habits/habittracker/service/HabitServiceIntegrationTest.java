package com.habits.habittracker.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.habits.habittracker.dto.request.HabitRequest;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HabitServiceIntegrationTest {

    @Autowired
    private HabitService habitService;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Habit testHabit;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos
        habitRepository.deleteAll();
        userRepository.deleteAll();

        // Crear y guardar usuario de prueba
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setNombre("Test User");
        testUser.setEmail("test@example.com");
        userRepository.save(testUser);

        // Crear hábito de prueba
        testHabit = new Habit();
        testHabit.setNombre("Ejercicio");
        testHabit.setCategoria("Salud");
        testHabit.setFrecuencia("Diario");
        testHabit.setHora("08:00");
        testHabit.setDescripcion("Hacer ejercicio por la mañana");
        testHabit.setUsuario(testUser);
        habitRepository.save(testHabit);

        // Configurar seguridad para el usuario de prueba
        setupSecurityContext(testUser.getUsername());
    }

    private void setupSecurityContext(String username) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getAllHabits_WhenHabitsExist_ReturnsAllHabits() {
        // Act
        List<Habit> habits = habitService.getAllHabits();

        // Assert
        assertNotNull(habits);
        assertFalse(habits.isEmpty());
        assertEquals(1, habits.size());
        assertEquals("Ejercicio", habits.get(0).getNombre());
        assertEquals(testUser.getId(), habits.get(0).getUsuario().getId());
    }

    @Test
    void getAllHabits_WhenNoHabitsExist_ReturnsEmptyList() {
        // Arrange
        habitRepository.deleteAll();

        // Act
        List<Habit> habits = habitService.getAllHabits();

        // Assert
        assertNotNull(habits);
        assertTrue(habits.isEmpty());
    }

    @Test
    void getHabitById_WhenHabitExists_ReturnsHabit() {
        // Act
        Optional<Habit> foundHabit = habitService.getHabitById(testHabit.getId());

        // Assert
        assertTrue(foundHabit.isPresent());
        assertEquals(testHabit.getId(), foundHabit.get().getId());
        assertEquals("Ejercicio", foundHabit.get().getNombre());
        assertEquals("08:00", foundHabit.get().getHora());
        assertEquals("Hacer ejercicio por la mañana", foundHabit.get().getDescripcion());
        assertEquals(testUser.getId(), foundHabit.get().getUsuario().getId());
    }

    @Test
    void getHabitById_WhenHabitDoesNotExist_ReturnsEmptyOptional() {
        // Act
        Optional<Habit> foundHabit = habitService.getHabitById(999L);

        // Assert
        assertFalse(foundHabit.isPresent());
    }

    @Test
    void createHabit_WithValidHabitAndAuthenticatedUser_CreatesHabitSuccessfully() {
        // Arrange
        Habit newHabit = new Habit();
        newHabit.setNombre("Leer");
        newHabit.setCategoria("Educación");
        newHabit.setFrecuencia("Diario");
        newHabit.setHora("20:00");
        newHabit.setDescripcion("Leer 30 minutos antes de dormir");

        // Act
        Habit createdHabit = habitService.createHabit(newHabit);

        // Assert
        assertNotNull(createdHabit);
        assertNotNull(createdHabit.getId());
        assertEquals("Leer", createdHabit.getNombre());
        assertEquals("Educación", createdHabit.getCategoria());
        assertEquals("Diario", createdHabit.getFrecuencia());
        assertEquals("20:00", createdHabit.getHora());
        assertEquals("Leer 30 minutos antes de dormir", createdHabit.getDescripcion());
        assertEquals(testUser.getId(), createdHabit.getUsuario().getId());
        assertEquals("testuser", createdHabit.getUsuario().getUsername());

        // Verificar que se guardó en la base de datos
        Optional<Habit> savedHabit = habitRepository.findById(createdHabit.getId());
        assertTrue(savedHabit.isPresent());
    }

    @Test
    void createHabit_WithValidHabitButNoHora_CreatesHabitSuccessfully() {
        // Arrange - Hora es opcional según las validaciones (@Pattern pero no @NotBlank)
        Habit newHabit = new Habit();
        newHabit.setNombre("Meditación");
        newHabit.setCategoria("Salud Mental");
        newHabit.setFrecuencia("Diario");
        // No establecer hora
        newHabit.setDescripcion("Meditar 10 minutos al día");

        // Act
        Habit createdHabit = habitService.createHabit(newHabit);

        // Assert
        assertNotNull(createdHabit);
        assertEquals("Meditación", createdHabit.getNombre());
        assertNull(createdHabit.getHora()); // Hora puede ser null
        assertEquals(testUser.getId(), createdHabit.getUsuario().getId());
    }

    @Test
    void createHabit_WithLongDescription_TruncatesOrValidatesCorrectly() {
        // Arrange
        String longDescription = "A".repeat(255); // Máximo permitido
        Habit newHabit = new Habit();
        newHabit.setNombre("Hábito con descripción larga");
        newHabit.setCategoria("Test");
        newHabit.setFrecuencia("Diario");
        newHabit.setHora("12:00");
        newHabit.setDescripcion(longDescription);

        // Act
        Habit createdHabit = habitService.createHabit(newHabit);

        // Assert
        assertNotNull(createdHabit);
        assertEquals(longDescription, createdHabit.getDescripcion());
        assertEquals(255, createdHabit.getDescripcion().length());
    }

    @Test
    void createHabit_WhenUserNotAuthenticated_ThrowsBadRequestException() {
        // Arrange
        SecurityContextHolder.clearContext();
        Habit newHabit = new Habit();
        newHabit.setNombre("Test Habit");
        newHabit.setCategoria("Test");
        newHabit.setFrecuencia("Diario");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> habitService.createHabit(newHabit)
        );

        assertEquals("No hay usuario autenticado en la sesión actual", exception.getMessage());
    }

    @Test
    void createHabit_WhenAuthenticatedUserNotFoundInDatabase_ThrowsResourceNotFoundException() {
        // Arrange
        setupSecurityContext("nonexistentuser");
        Habit newHabit = new Habit();
        newHabit.setNombre("Test Habit");
        newHabit.setCategoria("Test");
        newHabit.setFrecuencia("Diario");

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> habitService.createHabit(newHabit)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void createHabit_WithInvalidSecurityPrincipal_ThrowsBadRequestException() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("invalidPrincipal", null)
        );
        Habit newHabit = new Habit();
        newHabit.setNombre("Test Habit");
        newHabit.setCategoria("Test");
        newHabit.setFrecuencia("Diario");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> habitService.createHabit(newHabit)
        );

        assertEquals("No se pudo identificar el usuario autenticado", exception.getMessage());
    }

    @Test
    void updateHabit_WhenHabitExists_UpdatesHabitSuccessfully() {
        // Arrange
        HabitRequest updateRequest = new HabitRequest();
        updateRequest.setNombre("Ejercicio Modificado");
        updateRequest.setCategoria("Fitness");
        updateRequest.setFrecuencia("Semanal");
        updateRequest.setHora("09:00");
        updateRequest.setDescripcion("Hacer ejercicio los fines de semana");

        // Act
        Habit updatedHabit = habitService.updateHabit(testHabit.getId(), updateRequest);

        // Assert
        assertNotNull(updatedHabit);
        assertEquals(testHabit.getId(), updatedHabit.getId());
        assertEquals("Ejercicio Modificado", updatedHabit.getNombre());
        assertEquals("Fitness", updatedHabit.getCategoria());
        assertEquals("Semanal", updatedHabit.getFrecuencia());
        assertEquals("09:00", updatedHabit.getHora());
        assertEquals("Hacer ejercicio los fines de semana", updatedHabit.getDescripcion());
        
        // Verificar que el usuario no cambió
        assertEquals(testUser.getId(), updatedHabit.getUsuario().getId());
    }

    @Test
    void updateHabit_WithValidHoraFormat_UpdatesSuccessfully() {
        // Arrange - Probar diferentes formatos de hora válidos
        String[] validHours = {"09:00", "15:30", "23:59", "00:00", "12:00"};
        
        for (String hora : validHours) {
            HabitRequest updateRequest = new HabitRequest();
            updateRequest.setNombre("Hábito Test");
            updateRequest.setCategoria("Test");
            updateRequest.setFrecuencia("Diario");
            updateRequest.setHora(hora);
            updateRequest.setDescripcion("Descripción test");

            // Act
            Habit updatedHabit = habitService.updateHabit(testHabit.getId(), updateRequest);

            // Assert
            assertNotNull(updatedHabit);
            assertEquals(hora, updatedHabit.getHora());
        }
    }

    @Test
    void updateHabit_WhenHabitDoesNotExist_ThrowsResourceNotFoundException() {
        // Arrange
        HabitRequest updateRequest = new HabitRequest();
        updateRequest.setNombre("Hábito Inexistente");
        updateRequest.setCategoria("Test");
        updateRequest.setFrecuencia("Diario");

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> habitService.updateHabit(999L, updateRequest)
        );

        assertEquals("Hábito no encontrado con id: 999", exception.getMessage());
    }

    @Test
    void updateHabit_WithPartialUpdate_UpdatesOnlyProvidedFields() {
        // Arrange
        HabitRequest partialUpdate = new HabitRequest();
        partialUpdate.setNombre("Ejercicio Renombrado");
        partialUpdate.setCategoria("Salud Modificada");
        // No establecer frecuencia, hora ni descripción (vendrán como null)

        // Act
        Habit updatedHabit = habitService.updateHabit(testHabit.getId(), partialUpdate);

        // Assert
        assertNotNull(updatedHabit);
        assertEquals("Ejercicio Renombrado", updatedHabit.getNombre());
        assertEquals("Salud Modificada", updatedHabit.getCategoria());
        // Los campos no proporcionados deberían mantenerse con sus valores originales
        assertEquals("Diario", updatedHabit.getFrecuencia());
        assertEquals("08:00", updatedHabit.getHora());
        assertEquals("Hacer ejercicio por la mañana", updatedHabit.getDescripcion());
    }

    @Test
    void updateHabit_WithNullFieldsInRequest_DoesNotOverrideExistingValues() {
        // Arrange
        HabitRequest updateWithNulls = new HabitRequest();
        updateWithNulls.setNombre("Nuevo Nombre");
        // Categoría, frecuencia, hora y descripción se dejan como null

        // Act
        Habit updatedHabit = habitService.updateHabit(testHabit.getId(), updateWithNulls);

        // Assert
        assertNotNull(updatedHabit);
        assertEquals("Nuevo Nombre", updatedHabit.getNombre()); // Campo actualizado
        assertEquals("Salud", updatedHabit.getCategoria()); // Mantiene valor original
        assertEquals("Diario", updatedHabit.getFrecuencia()); // Mantiene valor original
        assertEquals("08:00", updatedHabit.getHora()); // Mantiene valor original
        assertEquals("Hacer ejercicio por la mañana", updatedHabit.getDescripcion()); // Mantiene valor original
    }

    @Test
    void updateHabit_WithEmptyHora_SetsHoraToEmpty() {
        // Arrange
        HabitRequest updateRequest = new HabitRequest();
        updateRequest.setNombre("Ejercicio");
        updateRequest.setCategoria("Salud");
        updateRequest.setFrecuencia("Diario");
        updateRequest.setHora(""); // Hora vacía
        updateRequest.setDescripcion("Descripción");

        // Act
        Habit updatedHabit = habitService.updateHabit(testHabit.getId(), updateRequest);

        // Assert
        assertNotNull(updatedHabit);
        assertEquals("", updatedHabit.getHora());
    }

    @Test
    void updateHabit_WithEmptyDescription_SetsDescriptionToEmpty() {
        // Arrange
        HabitRequest updateRequest = new HabitRequest();
        updateRequest.setNombre("Ejercicio");
        updateRequest.setCategoria("Salud");
        updateRequest.setFrecuencia("Diario");
        updateRequest.setHora("08:00");
        updateRequest.setDescripcion(""); // Descripción vacía

        // Act
        Habit updatedHabit = habitService.updateHabit(testHabit.getId(), updateRequest);

        // Assert
        assertNotNull(updatedHabit);
        assertEquals("", updatedHabit.getDescripcion());
    }

    @Test
    void deleteHabit_WhenHabitExists_DeletesHabitSuccessfully() {
        // Act
        habitService.deleteHabit(testHabit.getId());

        // Assert
        Optional<Habit> deletedHabit = habitRepository.findById(testHabit.getId());
        assertFalse(deletedHabit.isPresent());
        
        // Verificar que el usuario sigue existiendo
        assertTrue(userRepository.findById(testUser.getId()).isPresent());
    }

    @Test
    void deleteHabit_WhenHabitDoesNotExist_ThrowsResourceNotFoundException() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> habitService.deleteHabit(999L)
        );

        assertEquals("Hábito no encontrado con id: 999", exception.getMessage());
    }

    @Test
    void createHabit_WithMultipleUsers_CreatesHabitsForCorrectUser() {
        // Arrange - Crear segundo usuario
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setPassword("password2");
        secondUser.setNombre("Second User");
        secondUser.setEmail("second@example.com");
        userRepository.save(secondUser);

        // Crear hábito para el usuario actual (testUser)
        Habit habitForFirstUser = new Habit();
        habitForFirstUser.setNombre("Hábito Usuario 1");
        habitForFirstUser.setCategoria("Test");
        habitForFirstUser.setFrecuencia("Diario");
        Habit createdHabit1 = habitService.createHabit(habitForFirstUser);

        // Cambiar contexto de seguridad al segundo usuario
        setupSecurityContext("seconduser");

        // Crear hábito para el segundo usuario
        Habit habitForSecondUser = new Habit();
        habitForSecondUser.setNombre("Hábito Usuario 2");
        habitForSecondUser.setCategoria("Test");
        habitForSecondUser.setFrecuencia("Semanal");
        Habit createdHabit2 = habitService.createHabit(habitForSecondUser);

        // Assert
        assertEquals(testUser.getId(), createdHabit1.getUsuario().getId());
        assertEquals(secondUser.getId(), createdHabit2.getUsuario().getId());
        assertNotEquals(createdHabit1.getUsuario().getId(), createdHabit2.getUsuario().getId());

        // Verificar en la base de datos
        List<Habit> allHabits = habitRepository.findAll();
        assertEquals(3, allHabits.size()); // 1 del setUp + 2 nuevos
    }

    @Test
    void getAllHabits_ReturnsHabitsFromAllUsers() {
        // Arrange - Crear segundo usuario y hábito
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setPassword("password2");
        userRepository.save(secondUser);

        Habit secondHabit = new Habit();
        secondHabit.setNombre("Hábito Segundo Usuario");
        secondHabit.setCategoria("Test");
        secondHabit.setFrecuencia("Diario");
        secondHabit.setUsuario(secondUser);
        habitRepository.save(secondHabit);

        // Act
        List<Habit> allHabits = habitService.getAllHabits();

        // Assert
        assertEquals(2, allHabits.size());
        assertTrue(allHabits.stream().anyMatch(h -> h.getUsuario().getId().equals(testUser.getId())));
        assertTrue(allHabits.stream().anyMatch(h -> h.getUsuario().getId().equals(secondUser.getId())));
    }

    @Test
    void serviceContextLoads_AndDependenciesAreInjected() {
        assertNotNull(habitService);
        assertNotNull(habitRepository);
        assertNotNull(userRepository);
    }
}