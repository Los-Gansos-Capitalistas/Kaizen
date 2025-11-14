package com.habits.habittracker.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.habits.habittracker.dto.request.HabitRequest;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HabitService habitService;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------
    // ✅ getAllHabits()
    // -------------------------
    @Test
    void testGetAllHabits_ReturnsList() {
        Habit h1 = new Habit();
        h1.setNombre("Leer");
        Habit h2 = new Habit();
        h2.setNombre("Ejercicio");

        when(habitRepository.findAll()).thenReturn(List.of(h1, h2));

        List<Habit> result = habitService.getAllHabits();

        assertEquals(2, result.size());
        assertEquals("Leer", result.get(0).getNombre());
        verify(habitRepository).findAll();
    }

    // -------------------------
    // ✅ getHabitById()
    // -------------------------
    @Test
    void testGetHabitById_Found() {
        Habit habit = new Habit();
        habit.setId(1L);
        habit.setNombre("Meditación");

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        Optional<Habit> result = habitService.getHabitById(1L);
        assertTrue(result.isPresent());
        assertEquals("Meditación", result.get().getNombre());
        verify(habitRepository).findById(1L);
    }

    @Test
    void testGetHabitById_NotFound() {
        when(habitRepository.findById(10L)).thenReturn(Optional.empty());

        Optional<Habit> result = habitService.getHabitById(10L);
        assertTrue(result.isEmpty());
        verify(habitRepository).findById(10L);
    }

    // -------------------------
    // ✅ createHabit()
    // -------------------------
    @Test
    void testCreateHabit_Success() {
        // Simular autenticación
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        User userEntity = new User();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> {
            Habit h = invocation.getArgument(0);
            h.setId(100L);
            return h;
        });

        Habit newHabit = new Habit();
        newHabit.setNombre("Leer 10 minutos");
        Habit result = habitService.createHabit(newHabit);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(userEntity, result.getUsuario());
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void testCreateHabit_NoUserFound_Throws() {
        // Simular autenticación válida pero sin usuario en repositorio
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "ghost", "password", List.of()
        );
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        Habit habit = new Habit();
        habit.setNombre("Correr");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> habitService.createHabit(habit));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    // -------------------------
    // ✅ updateHabit()
    // -------------------------
    @Test
    void testUpdateHabit_Success() {
        Habit habit = new Habit();
        habit.setId(1L);
        habit.setNombre("Beber agua");

        HabitRequest request = new HabitRequest();
        request.setNombre("Caminar");
        request.setCategoria("Salud");
        request.setFrecuencia("Diaria");
        request.setHora("08:00");
        request.setDescripcion("Caminar 20 minutos");

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Habit updated = habitService.updateHabit(1L, request);

        assertNotNull(updated);
        assertEquals("Caminar", updated.getNombre());
        assertEquals("Salud", updated.getCategoria());
        assertEquals("Diaria", updated.getFrecuencia());
        assertEquals("08:00", updated.getHora());
        assertEquals("Caminar 20 minutos", updated.getDescripcion());
    }

    @Test
    void testUpdateHabit_NotFound_Throws() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());
        HabitRequest req = new HabitRequest();
        req.setNombre("Test");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> habitService.updateHabit(99L, req));
        assertEquals("Hábito no encontrado con id: 99", ex.getMessage());
    }

    // -------------------------
    // ✅ deleteHabit()
    // -------------------------
    @Test
    void testDeleteHabit_Success() {
        when(habitRepository.existsById(1L)).thenReturn(true);
        habitService.deleteHabit(1L);
        verify(habitRepository).deleteById(1L);
    }

    @Test
    void testDeleteHabit_NotFound_Throws() {
        when(habitRepository.existsById(5L)).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> habitService.deleteHabit(5L));
        assertEquals("Hábito no encontrado con id: 5", ex.getMessage());
    }
}
