package com.habits.habittracker.repository;

import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.habits.habittracker.model.User;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByHabito(Habit habit);
    Optional<Progress> findByHabitoAndFecha(Habit habit, LocalDate fecha);
    long countByHabitoUsuarioAndFechaAndCumplidoTrue(User usuario, LocalDate fecha);
    List<Progress> findByHabitoInAndFecha(List<Habit> habitos, LocalDate fecha);
    List<Progress> findByHabitoInAndFechaBetween(List<Habit> habitos, LocalDate startDate, LocalDate endDate);
    List<Progress> findByHabitoInOrderByFechaAsc(List<Habit> habitos);
    List<Progress> findByHabitoIdOrderByFechaAsc(Long habitId);
    List<Progress> findByHabitoIn(List<Habit> habitos);
}