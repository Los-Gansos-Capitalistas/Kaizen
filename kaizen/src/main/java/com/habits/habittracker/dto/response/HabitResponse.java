package com.habits.habittracker.dto.response;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HabitResponse {
    private Long id;
    private String nombre;
    private String categoria;
    private String frecuencia;
    private String hora;
    private String descripcion;
    // ✅ CAMPO NUEVO - Fecha objetivo
    private LocalDate fechaObjetivo;
    
    // Constructor sin fechaObjetivo para compatibilidad
    public HabitResponse(Long id, String nombre, String categoria, String frecuencia, String hora, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.frecuencia = frecuencia;
        this.hora = hora;
        this.descripcion = descripcion;
    }
    
    // Constructor completo
    public HabitResponse(Long id, String nombre, String categoria, String frecuencia, String hora, String descripcion, LocalDate fechaObjetivo) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.frecuencia = frecuencia;
        this.hora = hora;
        this.descripcion = descripcion;
        this.fechaObjetivo = fechaObjetivo;
    }
}