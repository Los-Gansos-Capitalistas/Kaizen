package com.habits.habittracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.habits.habittracker.model.NotificationSettings;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

    // Buscar configuración por el ID del usuario asociado
    Optional<NotificationSettings> findByUsuario_Id(Long userId);
}
