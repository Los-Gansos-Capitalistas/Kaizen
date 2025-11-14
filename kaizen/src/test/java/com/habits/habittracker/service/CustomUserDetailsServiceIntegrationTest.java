package com.habits.habittracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomUserDetailsServiceIntegrationTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPasswordHash");
        testUser.setNombre("Test User");
        testUser.setEmail("test@example.com");
        
        userRepository.save(testUser);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("$2a$10$encodedPasswordHash", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ThrowsUsernameNotFoundException() {
        String username = "nonexistentuser";

        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(username)
        );

        assertEquals("Usuario no encontrado: " + username, exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void loadUserByUsername_WithInvalidUsernames_ThrowsException(String invalidUsername) {
        assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(invalidUsername)
        );
    }

    @Test
    void loadUserByUsername_WithExactUsername_ReturnsCorrectUser() {
        User user2 = new User();
        user2.setUsername("UserWithCaps");
        user2.setPassword("pass2");
        userRepository.save(user2);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("UserWithCaps");
        assertNotNull(userDetails);
        assertEquals("UserWithCaps", userDetails.getUsername());
    }

    @Test
    void serviceContextLoads() {
        assertNotNull(customUserDetailsService);
        assertNotNull(userRepository);
    }
}