package com.habits.habittracker.controller;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habits.habittracker.dto.request.HabitRequest;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HabitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        habitRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("maria");
        user.setPassword("123");
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe crear un hábito correctamente con usuario autenticado y fechaObjetivo")
    void testCreateHabit_Success() throws Exception {
        HabitRequest request = new HabitRequest();
        request.setNombre("Beber agua");
        request.setCategoria("Salud");
        request.setFrecuencia("Diaria");
        request.setHora("08:00");
        request.setDescripcion("Beber 8 vasos de agua al día");
        request.setFechaObjetivo(LocalDate.of(2025, 12, 31));

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Beber agua"))
                .andExpect(jsonPath("$.categoria").value("Salud"))
                .andExpect(jsonPath("$.descripcion").value("Beber 8 vasos de agua al día"))
                .andExpect(jsonPath("$.fechaObjetivo").value("2025-12-31"));

        Habit saved = habitRepository.findAll().get(0);
        assertThat(saved.getUsuario().getUsername()).isEqualTo("maria");
        assertThat(saved.getFechaObjetivo()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe crear un hábito sin fechaObjetivo (campo opcional)")
    void testCreateHabit_WithoutFechaObjetivo() throws Exception {
        HabitRequest request = new HabitRequest();
        request.setNombre("Correr");
        request.setCategoria("Ejercicio");
        request.setFrecuencia("Diaria");
        request.setHora("07:00");
        request.setDescripcion("Correr 30 minutos");

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Correr"))
                .andExpect(jsonPath("$.fechaObjetivo").isEmpty());

        Habit saved = habitRepository.findAll().get(0);
        assertThat(saved.getFechaObjetivo()).isNull();
    }

    @Test
    @DisplayName("Debe fallar al crear hábito sin autenticación")
    void testCreateHabit_Unauthorized() throws Exception {
        HabitRequest request = new HabitRequest();
        request.setNombre("Correr");
        request.setCategoria("Salud");
        request.setFrecuencia("Diaria");

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe fallar si falta un campo obligatorio (nombre)")
    void testCreateHabit_BadRequest_MissingField() throws Exception {
        HabitRequest request = new HabitRequest();
        request.setCategoria("Personal");
        request.setFrecuencia("Semanal");

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe obtener todos los hábitos del usuario autenticado con fechaObjetivo")
    void testGetAllHabits_Success() throws Exception {
        User maria = userRepository.findByUsername("maria").get();
        
        Habit h1 = new Habit();
        h1.setNombre("Dormir bien");
        h1.setCategoria("Salud");
        h1.setFrecuencia("Diaria");
        h1.setHora("22:00");
        h1.setDescripcion("Dormir al menos 8 horas");
        h1.setFechaObjetivo(LocalDate.of(2025, 11, 30));
        h1.setUsuario(maria);
        habitRepository.save(h1);

        Habit h2 = new Habit();
        h2.setNombre("Caminar");
        h2.setCategoria("Ejercicio");
        h2.setFrecuencia("Diaria");
        h2.setHora("06:00");
        h2.setDescripcion("Caminar 30 minutos");
        h2.setFechaObjetivo(null);
        h2.setUsuario(maria);
        habitRepository.save(h2);

        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Dormir bien"))
                .andExpect(jsonPath("$[0].fechaObjetivo").value("2025-11-30"))
                .andExpect(jsonPath("$[1].nombre").value("Caminar"))
                .andExpect(jsonPath("$[1].fechaObjetivo").isEmpty());
    }

    @Test
    @DisplayName("Debe devolver 401 si se intenta acceder a hábitos sin autenticación")
    void testGetAllHabits_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe actualizar un hábito existente incluyendo fechaObjetivo")
    void testUpdateHabit_Success() throws Exception {
        User maria = userRepository.findByUsername("maria").get();
        Habit habit = new Habit();
        habit.setNombre("Leer");
        habit.setCategoria("Personal");
        habit.setFrecuencia("Diaria");
        habit.setHora("21:00");
        habit.setDescripcion("Leer 15 minutos");
        habit.setFechaObjetivo(LocalDate.of(2025, 10, 15));
        habit.setUsuario(maria);
        habitRepository.save(habit);

        HabitRequest update = new HabitRequest();
        update.setNombre("Leer 30 minutos");
        update.setCategoria("Personal");
        update.setFrecuencia("Diaria");
        update.setHora("21:30");
        update.setDescripcion("Leer más tiempo cada noche");
        update.setFechaObjetivo(LocalDate.of(2025, 12, 25));

        mockMvc.perform(put("/api/habits/" + habit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Leer 30 minutos"))
                .andExpect(jsonPath("$.descripcion").value("Leer más tiempo cada noche"))
                .andExpect(jsonPath("$.fechaObjetivo").value("2025-12-25"));

        Habit updatedHabit = habitRepository.findById(habit.getId()).get();
        assertThat(updatedHabit.getFechaObjetivo()).isEqualTo(LocalDate.of(2025, 12, 25));
    }

    @Test
@WithMockUser(username = "maria", roles = "USER")
@DisplayName("Debe actualizar eliminando la fechaObjetivo")
void testUpdateHabit_RemoveFechaObjetivo() throws Exception {
    User maria = userRepository.findByUsername("maria").get();
    Habit habit = new Habit();
    habit.setNombre("Meditar");
    habit.setCategoria("Bienestar");
    habit.setFrecuencia("Diaria");
    habit.setHora("07:00");
    habit.setDescripcion("Meditar 10 minutos");
    habit.setFechaObjetivo(LocalDate.of(2025, 11, 30));
    habit.setUsuario(maria);
    habitRepository.save(habit);

    // Crear request con fechaObjetivo explícitamente null
    HabitRequest updateRequest = new HabitRequest();
    updateRequest.setNombre("Meditar Actualizado");
    updateRequest.setCategoria("Bienestar");
    updateRequest.setFrecuencia("Diaria");
    updateRequest.setHora("07:00");
    updateRequest.setDescripcion("Meditar 10 minutos");
    updateRequest.setFechaObjetivo(null); // ESTO ES CLAVE

    mockMvc.perform(put("/api/habits/" + habit.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Meditar Actualizado"))
            .andExpect(jsonPath("$.fechaObjetivo").isEmpty()); // Debería estar vacío

    // Verificación CRÍTICA: en la base de datos
    Habit updatedHabit = habitRepository.findById(habit.getId()).get();
    assertThat(updatedHabit.getFechaObjetivo())
        .describedAs("El campo fechaObjetivo debería ser null después de la actualización")
        .isNull();
}

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe devolver 400 al intentar actualizar hábito inexistente")
    void testUpdateHabit_NotFound() throws Exception {
        HabitRequest update = new HabitRequest();
        update.setNombre("No existe");

        mockMvc.perform(put("/api/habits/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe eliminar un hábito existente")
    void testDeleteHabit_Success() throws Exception {
        User maria = userRepository.findByUsername("maria").get();
        Habit habit = new Habit();
        habit.setNombre("Meditar");
        habit.setCategoria("Bienestar");
        habit.setFrecuencia("Diaria");
        habit.setHora("07:00");
        habit.setDescripcion("Meditar 10 minutos");
        habit.setFechaObjetivo(LocalDate.of(2025, 12, 31));
        habit.setUsuario(maria);
        habitRepository.save(habit);

        mockMvc.perform(delete("/api/habits/" + habit.getId()))
                .andExpect(status().isNoContent());

        Optional<Habit> deleted = habitRepository.findById(habit.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe devolver 404 al intentar eliminar un hábito inexistente")
    void testDeleteHabit_NotFound() throws Exception {
        mockMvc.perform(delete("/api/habits/12345"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe marcar un hábito como completado")
    void testMarkHabitAsCompleted_Success() throws Exception {
        User maria = userRepository.findByUsername("maria").get();
        Habit habit = new Habit();
        habit.setNombre("Estudiar");
        habit.setCategoria("Educación");
        habit.setFrecuencia("Diaria");
        habit.setHora("19:00");
        habit.setDescripcion("Estudiar 1 hora");
        habit.setFechaObjetivo(LocalDate.of(2025, 12, 31));
        habit.setUsuario(maria);
        habitRepository.save(habit);

        String requestBody = "{\"completado\": true}";

        mockMvc.perform(post("/api/habits/" + habit.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Estudiar"))
                .andExpect(jsonPath("$.fechaObjetivo").value("2025-12-31"));
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe marcar un hábito como no completado")
    void testMarkHabitAsNotCompleted_Success() throws Exception {
        User maria = userRepository.findByUsername("maria").get();
        Habit habit = new Habit();
        habit.setNombre("Yoga");
        habit.setCategoria("Ejercicio");
        habit.setFrecuencia("Semanal");
        habit.setHora("08:00");
        habit.setDescripcion("Sesión de yoga");
        habit.setFechaObjetivo(null);
        habit.setUsuario(maria);
        habitRepository.save(habit);

        String requestBody = "{\"completado\": false}";

        mockMvc.perform(post("/api/habits/" + habit.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Yoga"))
                .andExpect(jsonPath("$.fechaObjetivo").isEmpty());
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe fallar al marcar hábito inexistente como completado")
    void testMarkHabitAsCompleted_NotFound() throws Exception {
        String requestBody = "{\"completado\": true}";

        mockMvc.perform(post("/api/habits/9999/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debe fallar al marcar hábito como completado sin autenticación")
    void testMarkHabitAsCompleted_Unauthorized() throws Exception {
        String requestBody = "{\"completado\": true}";

        mockMvc.perform(post("/api/habits/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe obtener un hábito por ID con fechaObjetivo")
    void testGetHabitById_Success() throws Exception {
        User maria = userRepository.findByUsername("maria").get();
        Habit habit = new Habit();
        habit.setNombre("Natación");
        habit.setCategoria("Deporte");
        habit.setFrecuencia("Semanal");
        habit.setHora("17:00");
        habit.setDescripcion("Nadar 1 hora");
        habit.setFechaObjetivo(LocalDate.of(2025, 11, 15));
        habit.setUsuario(maria);
        habitRepository.save(habit);

        mockMvc.perform(get("/api/habits/" + habit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Natación"))
                .andExpect(jsonPath("$.fechaObjetivo").value("2025-11-15"));
    }

    @Test
    @WithMockUser(username = "maria", roles = "USER")
    @DisplayName("Debe fallar al obtener hábito por ID inexistente")
    void testGetHabitById_NotFound() throws Exception {
        mockMvc.perform(get("/api/habits/9999"))
                .andExpect(status().isNotFound());
    }
}