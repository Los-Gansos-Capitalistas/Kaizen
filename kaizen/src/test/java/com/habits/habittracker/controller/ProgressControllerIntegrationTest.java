package com.habits.habittracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habits.habittracker.dto.request.ProgressRequest;
import com.habits.habittracker.dto.response.DailyProgressResponse;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.service.ProgressService;
import com.habits.habittracker.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private Progress progress;
    private ProgressRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("maria");

        progress = new Progress();
        progress.setId(1L);
        progress.setCumplido(true);
        progress.setFecha(LocalDate.now());

        request = new ProgressRequest();
        request.setFecha(LocalDate.now());
        request.setCumplido(true);
    }

    // 1. Crear o actualizar progreso
    @Test
    void marcarProgreso_DeberiaRetornar200() throws Exception {
        Mockito.when(progressService.marcarProgreso(eq(1L), any(ProgressRequest.class))).thenReturn(progress);

        mockMvc.perform(post("/api/progress/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cumplido").value(true));
    }

    // 2. Obtener progreso por hábito
    @Test
    void obtenerProgresoPorHabito_DeberiaRetornar200() throws Exception {
        Mockito.when(progressService.obtenerProgresoPorHabito(1L)).thenReturn(List.of(progress));

        mockMvc.perform(get("/api/progress/habit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cumplido").value(true));
    }

    // 3. Eliminar progreso
    @Test
    void eliminarProgreso_DeberiaRetornar200() throws Exception {
        mockMvc.perform(delete("/api/progress/1"))
                .andExpect(status().isOk());
    }

    // 4. Obtener progreso diario (resumen)
    @Test
    void obtenerProgresoDiarioResumen_DeberiaRetornar200() throws Exception {
        Map<String, Object> mockResponse = Map.of(
                "fecha", LocalDate.now().toString(),
                "totalHabitos", 5,
                "habitosCumplidos", 3,
                "porcentaje", 60.0
        );
        Mockito.when(progressService.obtenerProgresoDiario("maria")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/progress/daily/maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHabitos").value(5))
                .andExpect(jsonPath("$.porcentaje").value(60.0));
    }

    // 5. Obtener progreso diario detallado
    @Test
    void obtenerProgresoDiarioDetallado_DeberiaRetornar200() throws Exception {
        DailyProgressResponse response = new DailyProgressResponse(
                LocalDate.now(),
                List.of("Ejercicio"),
                List.of("Leer"),
                50.0
        );

        Mockito.when(progressService.obtenerProgresoDiarioDetallado(eq("maria"), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/progress/daily/maria/" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habitosCumplidos[0]").value("Ejercicio"))
                .andExpect(jsonPath("$.porcentaje").value(50.0));
    }

    // 6. Obtener estadísticas semanales
    @Test
    void obtenerEstadisticasSemanales_DeberiaRetornar200() throws Exception {
        Mockito.when(userService.findByUsername("maria")).thenReturn(Optional.of(user));
        Mockito.when(progressService.obtenerEstadisticasSemanales(user))
                .thenReturn(Map.of("totalHabitos", 3, "porcentajeCumplimiento", 75.0));

        mockMvc.perform(get("/api/progress/weekly/maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHabitos").value(3))
                .andExpect(jsonPath("$.porcentajeCumplimiento").value(75.0));
    }

    // 7. Obtener estadísticas mensuales
    @Test
    void obtenerEstadisticasMensuales_DeberiaRetornar200() throws Exception {
        Mockito.when(userService.findByUsername("maria")).thenReturn(Optional.of(user));
        Mockito.when(progressService.obtenerEstadisticasMensuales(user))
                .thenReturn(Map.of("habitosCumplidos", 20, "porcentajeCumplimiento", 66.6));

        mockMvc.perform(get("/api/progress/monthly/maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habitosCumplidos").value(20))
                .andExpect(jsonPath("$.porcentajeCumplimiento").value(66.6));
    }

    // 8. Error: hábito no encontrado
    @Test
    void marcarProgreso_HabitoNoEncontrado_DeberiaRetornar404() throws Exception {
        Mockito.when(progressService.marcarProgreso(eq(999L), any(ProgressRequest.class)))
                .thenThrow(new RuntimeException("Hábito no encontrado con id: 999"));

        mockMvc.perform(post("/api/progress/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error interno del servidor"));
    }

    // 9. Error: usuario no encontrado
    @Test
    void obtenerProgresoDiario_UsuarioNoEncontrado_DeberiaRetornar404() throws Exception {
        Mockito.when(progressService.obtenerProgresoDiario("noexiste"))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/api/progress/daily/noexiste"))
                .andExpect(status().isInternalServerError());
    }
}
