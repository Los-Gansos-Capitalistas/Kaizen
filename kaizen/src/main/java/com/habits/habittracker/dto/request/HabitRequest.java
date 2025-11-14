package com.habits.habittracker.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HabitRequest {

    @NotBlank(message = "El nombre del hábito es obligatorio.")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria.")
    private String categoria;

    @NotBlank(message = "La frecuencia es obligatoria.")
    private String frecuencia;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "La hora debe tener el formato HH:mm")
    private String hora;

    @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres.")
    private String descripcion;
    
    // CORREGIDO: Sin validaciones para permitir null
    private LocalDate fechaObjetivo;
}