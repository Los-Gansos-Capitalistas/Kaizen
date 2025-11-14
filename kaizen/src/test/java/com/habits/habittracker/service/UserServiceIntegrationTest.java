package com.habits.habittracker.service;

import com.habits.habittracker.dto.request.UserRequest;
import com.habits.habittracker.exception.ConflictException;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_DeberiaGuardarUsuarioEnBaseDeDatos() {
        UserRequest req = new UserRequest();
        req.setUsername("juan");
        req.setPassword("1234");
        req.setNombre("Juan Pablo");
        req.setEmail("juan@example.com");

        User registrado = userService.register(req);

        assertThat(registrado.getId()).isNotNull();
        assertThat(passwordEncoder.matches("1234", registrado.getPassword())).isTrue();

        Optional<User> encontrado = userRepository.findByUsername("juan");
        assertThat(encontrado).isPresent();
    }

    @Test
    void register_NoDebePermitirUsuarioDuplicado() {
        UserRequest req = new UserRequest();
        req.setUsername("juan");
        req.setPassword("abcd");
        req.setNombre("Juan Pablo");
        req.setEmail("juan@example.com");

        userService.register(req);

        assertThrows(ConflictException.class, () -> userService.register(req));
    }

    @Test
    void findByUsername_DeberiaRetornarUsuarioExistente() {
        User user = new User();
        user.setUsername("maria");
        user.setPassword(passwordEncoder.encode("4321"));
        userRepository.save(user);

        Optional<User> resultado = userService.findByUsername("maria");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo("maria");
    }
}