package com.habits.habittracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyProgressResponse {
    private LocalDate fecha;
    private List<String> habitosCumplidos;
    private List<String> habitosPendientes;
    private double porcentaje;
}