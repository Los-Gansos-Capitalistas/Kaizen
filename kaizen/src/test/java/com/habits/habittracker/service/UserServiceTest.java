package com.habits.habittracker.service;

import com.habits.habittracker.dto.request.UserRequest;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ConflictException;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRequest();
        request.setUsername("juan");
        request.setPassword("1234");
        request.setNombre("Juan Pablo");
        request.setEmail("juan@example.com");
    }

    @Test
    void register_DeberiaRegistrarUsuarioExitosamente() {
        when(userRepository.existsByUsername("juan")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hashed1234");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User resultado = userService.register(request);

        assertThat(resultado.getUsername()).isEqualTo("juan");
        assertThat(resultado.getPassword()).isEqualTo("hashed1234");
        assertThat(resultado.getEmail()).isEqualTo("juan@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DebeLanzarExcepcionSiRequestEsNull() {
        assertThrows(BadRequestException.class, () -> userService.register(null));
    }

    @Test
    void register_DebeLanzarExcepcionSiUsuarioYaExiste() {
        when(userRepository.existsByUsername("juan")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByUsername_DeberiaRetornarUsuario() {
        User user = new User();
        user.setUsername("juan");
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("juan");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("juan");
    }

    @Test
    void findByUsername_DeberiaRetornarVacioSiNoExiste() {
        when(userRepository.findByUsername("juan")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("juan");

        assertThat(result).isEmpty();
    }
}