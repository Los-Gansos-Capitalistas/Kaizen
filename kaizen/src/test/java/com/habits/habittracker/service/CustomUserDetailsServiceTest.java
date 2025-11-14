package com.habits.habittracker.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;

/**
 * Tests alineados con la implementación real de CustomUserDetailsService:
 * - usa org.springframework.security.core.userdetails.User con Collections.emptyList()
 * - no tiene autoridades
 * - constructor de User lanza IllegalArgumentException si password es null (pero puede aceptar "")
 */
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserExists() {
        User user = new User();
        user.setUsername("maria");
        user.setPassword("1234");

        when(userRepository.findByUsername("maria")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("maria");

        assertNotNull(details);
        assertEquals("maria", details.getUsername());
        assertEquals("1234", details.getPassword());

        // Según la implementación actual, NO hay autoridades (se pasa Collections.emptyList())
        assertTrue(details.getAuthorities().isEmpty(), "No debe tener roles ni autoridades");
        verify(userRepository, times(1)).findByUsername("maria");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("noExiste")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("noExiste"));
    }

    @Test
    void testLoadUserByUsername_SpecialChars() {
        User user = new User();
        user.setUsername("usu@rio_123");
        user.setPassword("pass");

        when(userRepository.findByUsername("usu@rio_123")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("usu@rio_123");

        assertEquals("usu@rio_123", details.getUsername());
        assertEquals("pass", details.getPassword());
        assertTrue(details.getAuthorities().isEmpty());
    }

    @Test
    void testLoadUserByUsername_EmptyPassword_ReturnsUserDetails() {
        // En esta versión de Spring el constructor acepta cadena vacía; por eso esperamos retorno exitoso.
        User user = new User();
        user.setUsername("sinPass");
        user.setPassword(""); // cadena vacía

        when(userRepository.findByUsername("sinPass")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("sinPass");

        assertNotNull(details);
        assertEquals("", details.getPassword());
        assertEquals("sinPass", details.getUsername());
        assertTrue(details.getAuthorities().isEmpty());
    }

    @Test
    void testLoadUserByUsername_NullPassword_Throws() {
        // El constructor sí lanza IllegalArgumentException si password == null
        User user = new User();
        user.setUsername("nullPass");
        user.setPassword(null);

        when(userRepository.findByUsername("nullPass")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> customUserDetailsService.loadUserByUsername("nullPass"));
    }

    @Test
    void testRepositoryInteraction() {
        User user = new User();
        user.setUsername("repoUser");
        user.setPassword("pass");
        when(userRepository.findByUsername("repoUser")).thenReturn(Optional.of(user));

        customUserDetailsService.loadUserByUsername("repoUser");

        verify(userRepository, times(1)).findByUsername("repoUser");
        verifyNoMoreInteractions(userRepository);
    }
}
