package com.habits.habittracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida (response) para enviar logros al cliente.
 * Se utiliza en AchievementController.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private boolean obtenido;
    private String fechaObtencion;
}
