package com.habits.habittracker.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProgressRequest {
    private LocalDate fecha;
    private boolean cumplido;
}