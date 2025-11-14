package com.habits.habittracker.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.habits.habittracker.dto.request.ProgressRequest;
import com.habits.habittracker.dto.response.DailyProgressResponse;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.service.ProgressService;
import com.habits.habittracker.service.UserService;

class ProgressControllerTest {

    @Mock
    private ProgressService progressService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProgressController progressController;

    private ProgressRequest progressRequest;
    private Progress progress;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("juan");

        progressRequest = new ProgressRequest();
        progressRequest.setFecha(LocalDate.now());
        progressRequest.setCumplido(true);

        progress = new Progress();
        progress.setId(1L);
        progress.setCumplido(true);
    }

    @Test
    void marcarProgreso_DeberiaRetornarProgreso() {
        when(progressService.marcarProgreso(1L, progressRequest)).thenReturn(progress);

        Progress resultado = progressController.marcarProgreso(1L, progressRequest);

        assertNotNull(resultado);
        assertTrue(resultado.isCumplido());
        verify(progressService, times(1)).marcarProgreso(1L, progressRequest);
    }

    @Test
    void obtenerPorHabito_DeberiaRetornarLista() {
        when(progressService.obtenerProgresoPorHabito(1L)).thenReturn(List.of(progress));

        List<Progress> lista = progressController.obtenerPorHabito(1L);

        assertEquals(1, lista.size());
        assertTrue(lista.get(0).isCumplido());
        verify(progressService).obtenerProgresoPorHabito(1L);
    }

    @Test
    void eliminarProgreso_DeberiaLlamarServicio() {
        doNothing().when(progressService).eliminarProgreso(1L);

        progressController.eliminarProgreso(1L);

        verify(progressService, times(1)).eliminarProgreso(1L);
    }

    @Test
    void obtenerProgresoDiario_DeberiaRetornarMapa() {
        Map<String, Object> mockMap = Map.of("estado", "ok");
        when(progressService.obtenerProgresoDiario("juan")).thenReturn(mockMap);

        Map<String, Object> resultado = progressController.obtenerProgresoDiario("juan");

        assertEquals("ok", resultado.get("estado"));
        verify(progressService).obtenerProgresoDiario("juan");
    }

    @Test
    void obtenerProgresoDetallado_DeberiaRetornarResponse() {
        DailyProgressResponse mockResponse = new DailyProgressResponse();
        when(progressService.obtenerProgresoDiarioDetallado(eq("juan"), any(LocalDate.class))).thenReturn(mockResponse);

        DailyProgressResponse resultado = progressController.obtenerProgresoDetallado("juan", LocalDate.now().toString());

        assertNotNull(resultado);
        verify(progressService).obtenerProgresoDiarioDetallado(eq("juan"), any(LocalDate.class));
    }

    @Test
    void obtenerEstadisticasSemanales_DeberiaRetornarMapa() {
        Map<String, Object> mockMap = Map.of("semana", "ok");
        when(userService.findByUsername("juan")).thenReturn(Optional.of(user));
        when(progressService.obtenerEstadisticasSemanales(user)).thenReturn(mockMap);

        Map<String, Object> resultado = progressController.obtenerEstadisticasSemanales("juan");

        assertEquals("ok", resultado.get("semana"));
        verify(progressService).obtenerEstadisticasSemanales(user);
    }

    @Test
    void obtenerEstadisticasSemanales_DeberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(userService.findByUsername("noExiste")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> progressController.obtenerEstadisticasSemanales("noExiste"));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void obtenerEstadisticasMensuales_DeberiaRetornarMapa() {
        Map<String, Object> mockMap = Map.of("mes", "ok");
        when(userService.findByUsername("juan")).thenReturn(Optional.of(user));
        when(progressService.obtenerEstadisticasMensuales(user)).thenReturn(mockMap);

        Map<String, Object> resultado = progressController.obtenerEstadisticasMensuales("juan");

        assertEquals("ok", resultado.get("mes"));
        verify(progressService).obtenerEstadisticasMensuales(user);
    }

    @Test
    void obtenerEstadisticasMensuales_DeberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(userService.findByUsername("noExiste")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> progressController.obtenerEstadisticasMensuales("noExiste"));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }
}
