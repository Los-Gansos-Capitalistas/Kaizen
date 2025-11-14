package com.habits.habittracker.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.habits.habittracker.exception.BadRequestException;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.exception.UserNotFoundException;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.NotificationSettingsRepository;
import com.habits.habittracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;

    public NotificationSettings getSettings(Long userId) {
        if (userId == null) {
            throw new BadRequestException("El ID del usuario no puede ser nulo.");
        }

        // ✅ Si el usuario no existe, lanza UserNotFoundException
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return notificationSettingsRepository.findByUsuario_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontraron configuraciones de notificación para este usuario."));
    }

    public NotificationSettings saveSettings(Long userId, NotificationSettings newSettings) {
        if (userId == null) {
            throw new BadRequestException("El ID del usuario no puede ser nulo.");
        }

        if (newSettings == null) {
            throw new BadRequestException("La configuración de notificaciones no puede ser nula.");
        }

        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        NotificationSettings existingSettings = notificationSettingsRepository.findByUsuario_Id(userId)
                .orElse(NotificationSettings.builder().usuario(usuario).build());

        existingSettings.setEmailEnabled(newSettings.getEmailEnabled());
        existingSettings.setPushEnabled(newSettings.getPushEnabled());
        existingSettings.setSmsEnabled(newSettings.getSmsEnabled());
        existingSettings.setNotificationTime(newSettings.getNotificationTime());

        return notificationSettingsRepository.save(existingSettings);
    }

    public void deleteSettings(Long userId) {
        if (userId == null) {
            throw new BadRequestException("El ID del usuario no puede ser nulo.");
        }

        // ✅ Si el usuario no existe, lanza UserNotFoundException
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Optional<NotificationSettings> settings = notificationSettingsRepository.findByUsuario_Id(userId);
        if (settings.isEmpty()) {
            throw new ResourceNotFoundException("No existe configuración de notificaciones para este usuario.");
        }

        notificationSettingsRepository.delete(settings.get());
    }

    public NotificationSettings changeNotificationStatus(Long userId, boolean enabled) {
        NotificationSettings settings = getSettings(userId);
        settings.setEmailEnabled(enabled);
        settings.setPushEnabled(enabled);
        settings.setSmsEnabled(enabled);
        return notificationSettingsRepository.save(settings);
    }

    public NotificationSettings resetToDefault(Long userId) {
        if (userId == null) {
            throw new BadRequestException("El ID del usuario no puede ser nulo.");
        }

        // ✅ Cambiar ResourceNotFoundException a UserNotFoundException por consistencia
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        NotificationSettings defaultSettings = NotificationSettings.builder()
                .usuario(usuario)
                .emailEnabled(true)
                .pushEnabled(true)
                .smsEnabled(false)
                .notificationTime("08:00")
                .build();

        return notificationSettingsRepository.save(defaultSettings);
    }
}
