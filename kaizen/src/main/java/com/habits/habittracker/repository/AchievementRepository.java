package com.habits.habittracker.repository;

import com.habits.habittracker.model.Achievement;
import com.habits.habittracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUsuario(User usuario);
    boolean existsByUsuarioAndNombre(User usuario, String nombre);
}