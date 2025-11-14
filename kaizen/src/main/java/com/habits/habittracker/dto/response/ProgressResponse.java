package com.habits.habittracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    private Long id;
    private LocalDate fecha;
    private boolean cumplido;
    private Long habitId;
}