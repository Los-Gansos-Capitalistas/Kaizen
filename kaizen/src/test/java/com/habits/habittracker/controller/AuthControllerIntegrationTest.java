package com.habits.habittracker.controller;

import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habits.habittracker.dto.request.LoginRequest;
import com.habits.habittracker.dto.request.RegisterRequest;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.NotificationSettingsRepository;
import com.habits.habittracker.repository.ProgressRepository;
import com.habits.habittracker.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiarBD() {
        progressRepository.deleteAll();
        habitRepository.deleteAll();
        notificationSettingsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Registro exitoso devuelve 200 y token")
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevoUsuario");
        request.setPassword("123456");
        request.setEmail("nuevo@correo.com");
        request.setNombre("Nuevo Usuario");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register - Usuario duplicado devuelve 400")
    void testRegister_DuplicateUsername_ReturnsBadRequest() throws Exception {
        User user = new User();
        user.setUsername("usuarioExistente");
        user.setPassword(passwordEncoder.encode("abcdef"));
        user.setEmail("existente@correo.com");
        user.setNombre("Usuario Existente");
        userRepository.save(user);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("usuarioExistente");
        request.setPassword("abcdef");
        request.setEmail("existente@correo.com");
        request.setNombre("Intento duplicado");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El nombre de usuario ya está en uso"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Campos inválidos devuelve 400")
    void testRegister_InvalidData_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setPassword("");
        request.setEmail("");
        request.setNombre("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Login exitoso devuelve 200 y token")
    void testLogin_Success() throws Exception {
        User user = new User();
        user.setUsername("loginUser");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setEmail("login@correo.com");
        user.setNombre("Login User");
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("loginUser");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/login - Usuario inexistente devuelve 404")
    void testLogin_UserNotFound_ReturnsNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("noExiste");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Contraseña inválida devuelve 401")
    void testLogin_InvalidPassword_ReturnsUnauthorized() throws Exception {
        User user = new User();
        user.setUsername("wrongUser");
        user.setPassword(passwordEncoder.encode("correcta"));
        user.setEmail("wrong@correo.com");
        user.setNombre("Wrong User");
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("wrongUser");
        request.setPassword("incorrecta");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }
}
