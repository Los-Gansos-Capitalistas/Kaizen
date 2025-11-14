package com.habits.habittracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.habits.habittracker.model.Achievement;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.AchievementRepository;
import com.habits.habittracker.repository.UserRepository;
import com.habits.habittracker.service.AchievementService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AchievementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private AchievementService achievementService;

    private User usuarioTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new User();
        usuarioTest.setUsername("usuarioTest");
        usuarioTest.setPassword("1234");
        usuarioTest.setEmail("usuario@test.com");
        userRepository.save(usuarioTest);
    }

    // -----------------------------------
    // GET /api/achievements
    // -----------------------------------

    @Test
    @WithMockUser(username = "usuarioTest")
    @DisplayName("GET /api/achievements debería retornar lista vacía al inicio")
    void getAchievements_DeberiaRetornarListaVacia() throws Exception {
        mockMvc.perform(get("/api/achievements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "usuarioInexistente")
    @DisplayName("GET /api/achievements debería retornar 404 si usuario no existe")
    void getAchievements_DeberiaRetornar404SiUsuarioNoExiste() throws Exception {
        mockMvc.perform(get("/api/achievements"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }

    // -----------------------------------
    // POST /api/achievements/check
    // -----------------------------------

    @Test
    @WithMockUser(username = "usuarioTest")
    @DisplayName("POST /api/achievements/check debería asignar logros correctamente")
    void checkAchievements_DeberiaAsignarLogrosCorrectos() throws Exception {
        Achievement logro = new Achievement();
        logro.setNombre("Primer Hábito");
        logro.setDescripcion("Completa tu primer hábito.");
        achievementRepository.save(logro);

        achievementService.verificarYAsignarLogros(usuarioTest);

        mockMvc.perform(post("/api/achievements/check"))
                .andExpect(status().isOk())
                .andExpect(content().string("Verificación completada. Nuevos logros asignados si correspondía."));
    }

    @Test
    @WithMockUser(username = "usuarioInexistente")
    @DisplayName("POST /api/achievements/check debería retornar 404 si usuario no existe")
    void checkAchievements_DeberiaRetornar404SiUsuarioNoExiste() throws Exception {
        mockMvc.perform(post("/api/achievements/check"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
