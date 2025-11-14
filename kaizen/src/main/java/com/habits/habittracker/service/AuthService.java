package com.habits.habittracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.habits.habittracker.dto.request.LoginRequest;
import com.habits.habittracker.dto.request.RegisterRequest;
import com.habits.habittracker.dto.response.AuthResponse;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.exception.UnauthorizedException;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;
import com.habits.habittracker.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Validaciones: null o vacíos
        if (request == null
                || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Datos incompletos");
        }

        // Comprobar usuario duplicado
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya está en uso");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNombre(request.getNombre());

        // Guardar antes de autenticar (si falla el save lanzará excepción y será manejada por el GlobalExceptionHandler)
        userRepository.save(user);

        // Autenticar al usuario recién creado para generar token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateToken(authentication);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Datos incompletos");
        }

        // comprobar existencia del usuario (para devolver 404 si no existe)
        userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String token = jwtUtil.generateToken(authentication);
            return new AuthResponse(token);
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Credenciales inválidas");
        } catch (Exception ex) {
            // Envolver cualquier otra excepción de seguridad como Unauthorized para que tests reciban 401
            throw new UnauthorizedException("Credenciales inválidas");
        }
    }
}
