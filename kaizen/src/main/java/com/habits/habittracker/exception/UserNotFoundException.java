package com.habits.habittracker.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("Usuario con ID " + userId + " no encontrado.");
    }
}
