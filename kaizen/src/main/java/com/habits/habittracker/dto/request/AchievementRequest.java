package com.habits.habittracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada (request) para crear o actualizar logros manualmente
 * o para verificar logros desde el cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementRequest {
    private Long userId;         // ID del usuario al que pertenece el logro
    private String nombre;       // Nombre del logro
    private String descripcion;  // Descripción breve
    private boolean obtenido;    // Si ya fue desbloqueado
}
