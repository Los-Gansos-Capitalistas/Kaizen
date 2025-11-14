package com.habits.habittracker.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.habits.habittracker.dto.request.UserRequest;
import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ConflictException;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(UserRequest req) {
        if (req == null) {
            throw new BadRequestException("La solicitud de registro no puede ser nula.");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException("El nombre de usuario ya existe.");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNombre(req.getNombre());
        user.setEmail(req.getEmail());

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
