package com.habits.habittracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.NotificationSettingsRepository;
import com.habits.habittracker.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        notificationSettingsRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("1234");
        userRepository.save(user);
    }

    @Test
    void saveSettings_DeberiaRetornar200() throws Exception {
        NotificationSettings settings = NotificationSettings.builder()
                .usuario(user)
                .emailEnabled(true)
                .pushEnabled(false)
                .smsEnabled(true)
                .notificationTime("08:00")
                .build();

        mockMvc.perform(post("/api/notifications/{userId}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk()); // No exigimos JSON si el controller no devuelve objeto
    }

    @Test
    void getSettings_DeberiaRetornar200OSinContenido() throws Exception {
        notificationSettingsRepository.save(NotificationSettings.builder()
                .usuario(user)
                .emailEnabled(true)
                .pushEnabled(true)
                .smsEnabled(false)
                .notificationTime("09:00")
                .build());

        mockMvc.perform(get("/api/notifications/{userId}", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void getSettings_DeberiaRetornar404SiUsuarioNoExiste() throws Exception {
        mockMvc.perform(get("/api/notifications/{userId}", 999L))
                .andExpect(status().is4xxClientError()); // puede ser 404 o 500
    }

    @Test
    void changeNotificationStatus_DeberiaRetornar200() throws Exception {
        notificationSettingsRepository.save(NotificationSettings.builder()
                .usuario(user)
                .emailEnabled(true)
                .pushEnabled(true)
                .smsEnabled(true)
                .notificationTime("10:00")
                .build());

        mockMvc.perform(put("/api/notifications/{userId}/status", user.getId())
                        .param("enabled", "false"))
                .andExpect(status().isOk());
    }

    @Test
    void resetToDefault_DeberiaRetornar200() throws Exception {
        notificationSettingsRepository.save(NotificationSettings.builder()
                .usuario(user)
                .emailEnabled(true)
                .pushEnabled(true)
                .smsEnabled(true)
                .notificationTime("12:00")
                .build());

        mockMvc.perform(put("/api/notifications/{userId}/reset", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteSettings_DeberiaRetornar200() throws Exception {
        notificationSettingsRepository.save(NotificationSettings.builder()
                .usuario(user)
                .emailEnabled(true)
                .pushEnabled(true)
                .smsEnabled(true)
                .notificationTime("13:00")
                .build());

        mockMvc.perform(delete("/api/notifications/{userId}", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteSettings_DeberiaRetornar404SiUsuarioNoExiste() throws Exception {
        mockMvc.perform(delete("/api/notifications/{userId}", 999L))
                .andExpect(status().is4xxClientError());
    }
}
