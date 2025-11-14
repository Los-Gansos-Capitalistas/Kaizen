package com.habits.habittracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String nombre;
    private String email;
}