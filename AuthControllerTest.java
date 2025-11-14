package com.habits.habittracker.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.habits.habittracker.dto.request.LoginRequest;
import com.habits.habittracker.dto.request.RegisterRequest;
import com.habits.habittracker.dto.response.AuthResponse;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.exception.UnauthorizedException;
import com.habits.habittracker.service.AuthService;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Prueba: registro exitoso
    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevo");
        request.setPassword("1234");
        request.setEmail("nuevo@correo.com");
        request.setNombre("Nuevo User");

        // Usar el constructor que existe - solo token
        AuthResponse authResponse = new AuthResponse("fakeToken");

        when(authService.register(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("fakeToken", response.getBody().getToken());
        verify(authService).register(request);
    }

    // Prueba: registro con username duplicado
    @Test
    void testRegister_DuplicateUsername_ThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existente");
        request.setPassword("1234");
        request.setEmail("mail@test.com");
        request.setNombre("User");

        when(authService.register(request))
            .thenThrow(new BadRequestException("El usuario ya existe"));

        assertThrows(BadRequestException.class, () -> authController.register(request));
    }

    // Prueba: login exitoso
    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("1234");

        // Usar el constructor que existe - solo token
        AuthResponse authResponse = new AuthResponse("jwtToken");

        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("jwtToken", response.getBody().getToken());
        verify(authService).login(request);
    }

    // Prueba: login con usuario inexistente
    @Test
    void testLogin_UserNotFound_ThrowsResourceNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("noExiste");
        request.setPassword("1234");

        when(authService.login(request))
            .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

        assertThrows(ResourceNotFoundException.class, () -> authController.login(request));
    }

    // Prueba: login con credenciales inválidas
    @Test
    void testLogin_InvalidCredentials_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("wrong");

        when(authService.login(request))
            .thenThrow(new UnauthorizedException("Credenciales inválidas"));

        assertThrows(UnauthorizedException.class, () -> authController.login(request));
    }

    // Prueba: registro genera token después de guardar usuario
    @Test
    void testRegister_GeneratesToken_AfterSave() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevoUser");
        request.setPassword("pass");
        request.setEmail("email@test.com");
        request.setNombre("Nombre");

        // Usar el constructor que existe - solo token
        AuthResponse authResponse = new AuthResponse("generatedToken");

        when(authService.register(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertNotNull(response.getBody());
        assertEquals("generatedToken", response.getBody().getToken());
        verify(authService).register(request);
    }

    // Prueba: status endpoint funciona
    @Test
    void testStatus_ReturnsOk() {
        ResponseEntity<String> response = authController.status();
        
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        // Verificar que contiene el texto esperado
        assert(response.getBody().contains("success"));
        assert(response.getBody().contains("HabitTracker Auth API"));
    }
}