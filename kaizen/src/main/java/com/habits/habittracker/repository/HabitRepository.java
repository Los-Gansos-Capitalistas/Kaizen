package com.habits.habittracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.User;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUsuario(User usuario);
}
